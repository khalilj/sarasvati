/*
    This file is part of Sarasvati.

    Sarasvati is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Sarasvati is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Sarasvati.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2008-2009 Paul Lorenz
*/
package com.googlecode.sarasvati.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.googlecode.sarasvati.Arc;
import com.googlecode.sarasvati.ArcToken;
import com.googlecode.sarasvati.ArcTokenSetMember;
import com.googlecode.sarasvati.CustomNode;
import com.googlecode.sarasvati.Engine;
import com.googlecode.sarasvati.ExecutionType;
import com.googlecode.sarasvati.Graph;
import com.googlecode.sarasvati.GraphProcess;
import com.googlecode.sarasvati.GuardResponse;
import com.googlecode.sarasvati.JoinResult;
import com.googlecode.sarasvati.Node;
import com.googlecode.sarasvati.NodeToken;
import com.googlecode.sarasvati.NodeTokenSetMember;
import com.googlecode.sarasvati.ProcessState;
import com.googlecode.sarasvati.SarasvatiException;
import com.googlecode.sarasvati.TokenSet;
import com.googlecode.sarasvati.env.Env;
import com.googlecode.sarasvati.env.TokenSetMemberEnv;
import com.googlecode.sarasvati.event.ArcTokenEvent;
import com.googlecode.sarasvati.event.DefaultExecutionEventQueue;
import com.googlecode.sarasvati.event.EventActionType;
import com.googlecode.sarasvati.event.EventActions;
import com.googlecode.sarasvati.event.ExecutionEvent;
import com.googlecode.sarasvati.event.ExecutionEventType;
import com.googlecode.sarasvati.event.ExecutionListener;
import com.googlecode.sarasvati.event.NodeTokenEvent;
import com.googlecode.sarasvati.event.ProcessEvent;
import com.googlecode.sarasvati.rubric.RubricInterpreter;
import com.googlecode.sarasvati.rubric.env.DefaultRubricEnv;
import com.googlecode.sarasvati.rubric.env.DefaultRubricFunctionRepository;
import com.googlecode.sarasvati.rubric.env.RubricEnv;
import com.googlecode.sarasvati.script.ScriptEnv;
import com.googlecode.sarasvati.visitor.BacktrackTokenVisitor;
import com.googlecode.sarasvati.visitor.TokenTraversals;

/**
 * Contains all the engine logic which is not backend specific.
 *
 * @author Paul Lorenz
 */
public abstract class BaseEngine implements Engine
{
  protected static final String DEFAULT_APPLICATION_CONTEXT = "";
  protected static final Map<String, DefaultExecutionEventQueue> globalQueueMap = new ConcurrentHashMap<String, DefaultExecutionEventQueue>();

  protected boolean arcExecutionStarted = false;
  protected List<ArcToken> asyncQueue = new LinkedList<ArcToken>();

  protected final DefaultExecutionEventQueue globalEventQueue;
  protected BaseEngine parentEngine;

  /**
   * Creates a new Engine with the given application context.
   * Each application context has it's own set of global listeners.
   *
   * This allows different applications running the same JVM to
   * have different sets of listeners without having to add
   * them at the process level.

   * @param applicationContext The application context
   */
  public BaseEngine (final String applicationContext)
  {
    globalEventQueue = getGlobalQueueForContext( applicationContext );
  }

  private DefaultExecutionEventQueue getGlobalQueueForContext (final String applicationContext)
  {
    DefaultExecutionEventQueue queue = globalQueueMap.get( applicationContext );
    if ( queue == null )
    {
      queue = DefaultExecutionEventQueue.newCopyOnWriteListInstance();
      contributeGlobalListeners( queue );
      globalQueueMap.put( applicationContext, queue );
    }

    return queue;
  }

  /**
   * Provides a subclass to override which execution event listeners are added to
   * new global queues. By default this just adds the {@link TokenSetCompletionListener}.
   *
   * @param queue The new global queue
   */
  protected void contributeGlobalListeners (final DefaultExecutionEventQueue queue)
  {
    queue.addListener( new TokenSetCompletionListener(),
                       ExecutionEventType.ARC_TOKEN_COMPLETED,
                       ExecutionEventType.NODE_TOKEN_COMPLETED );
  }

  @Override
  public GraphProcess startProcess (final String graphName)
  {
    Graph graph = getRepository().getLatestGraph( graphName );
    if ( graph == null )
    {
      throw new SarasvatiException( "No graph found with name '" + graphName + "'" );
    }

    return startProcess( graph );
  }

  @Override
  public GraphProcess startProcess (final Graph graph)
  {
    GraphProcess process = getFactory().newProcess( graph );
    startProcess( process );
    return process;
  }

  @Override
  public void startProcess (final GraphProcess process)
  {
    process.setState( ProcessState.Executing );
    ProcessEvent.fireStartedEvent( this, process );

    arcExecutionStarted = true;

    try
    {
      for ( Node startNode : process.getGraph().getStartNodes() )
      {
        NodeToken startToken = getFactory().newNodeToken( process, startNode,
                                                          new ArrayList<ArcToken>(0) );
        NodeTokenEvent.fireCreatedEvent(this, startToken);
        process.addNodeToken( startToken );
        executeNode( process, startToken );
      }
      executeQueuedArcTokens( process );
    }
    finally
    {
      arcExecutionStarted = false;
      drainAsyncQueue( process );
    }

    if ( process.isExecuting() )
    {
      checkForCompletion( process );
    }
  }

  @Override
  public void cancelProcess (final GraphProcess process)
  {
    process.setState( ProcessState.PendingCancel );
    EventActions actions = ProcessEvent.fireCanceledEvent( this, process );

    if ( !actions.isEventTypeIncluded( EventActionType.DELAY_PROCESS_FINALIZE_CANCEL ) )
    {
      finalizeCancel( process );
    }
  }

  @Override
  public void finalizeComplete (final GraphProcess process)
  {
    process.setState( ProcessState.Completed );

    NodeToken parentToken = process.getParentToken();
    if ( parentToken != null )
    {
      Engine engine = getParentEngine() == null ? newEngine( false ) : getParentEngine();
      engine.complete( parentToken, Arc.DEFAULT_ARC );
    }
  }

  @Override
  public void finalizeCancel (final GraphProcess process)
  {
    process.setState( ProcessState.Canceled );
  }

  private void executeArc (final GraphProcess process,
                           final ArcToken token)
  {
    if ( token.isPending() )
    {
      token.markProcessed();
      ArcTokenEvent.fireProcessedEvent( this, token );

      process.addActiveArcToken( token );

      Node targetNode = token.getArc().getEndNode();
      JoinResult result = targetNode.getJoinStrategy( token.getArc() ).performJoin( this, process, token );

      if ( result.isJoinComplete() )
      {
        completeExecuteArc( process, targetNode, result.getArcTokensCompletingJoin() );
      }
    }
  }

  private void completeExecuteArc (final GraphProcess process,
                                   final Node targetNode,
                                   final List<ArcToken> tokens)
  {
    NodeToken nodeToken = getFactory().newNodeToken( process, targetNode, tokens );
    process.addNodeToken( nodeToken );

    // Add new node token to add the token sets which its generating arc tokens are members of
    Set<TokenSet> tokenSets = new HashSet<TokenSet>();

    for ( ArcToken token : tokens )
    {
      for ( ArcTokenSetMember setMember : token.getTokenSetMemberships() )
      {
        TokenSet tokenSet = setMember.getTokenSet();
        if ( !tokenSet.isComplete() && !tokenSets.contains( tokenSet ) )
        {
          tokenSets.add( tokenSet );
          NodeTokenSetMember newSetMember = getFactory().newNodeTokenSetMember( tokenSet, nodeToken, setMember.getMemberIndex() );
          tokenSet.getActiveNodeTokens( this ).add( nodeToken );
          nodeToken.getTokenSetMemberships().add( newSetMember );
        }
      }
    }

    NodeTokenEvent.fireCreatedEvent( this, nodeToken );

    for ( ArcToken token : tokens )
    {
      process.removeActiveArcToken( token );
      if ( token.isPending() )
      {
        token.markProcessed();
        ArcTokenEvent.fireProcessedEvent( this, token );
      }
      token.markComplete( nodeToken );
      ArcTokenEvent.fireCompletedEvent( this, token );
    }

    executeNode( process, nodeToken );
  }

  protected void executeNode (final GraphProcess process, final NodeToken token)
  {
    GuardResponse response = token.getNode().guard( this, token );
    token.setGuardAction( response.getGuardAction() );

    switch ( response.getGuardAction() )
    {
      case AcceptToken :
        process.addActiveNodeToken( token );
        EventActions actions = NodeTokenEvent.fireAcceptedEvent( this, token );
        if ( !actions.isEventTypeIncluded( EventActionType.DELAY_NODE_EXECUTION ) )
        {
          token.getNode().execute( this, token );
          NodeTokenEvent.fireExecutedEvent( this, token );
        }
        break;

      case DiscardToken :
        token.markComplete();
        NodeTokenEvent.fireDiscardedEvent( this, token );
        NodeTokenEvent.fireCompletedEvent( this, token, null );
        break;

      case SkipNode :
        process.addActiveNodeToken( token );
        NodeTokenEvent.fireSkippedEvent( this, token, response.getExitArcForSkip() );
        complete( token, response.getExitArcForSkip() );
        break;
    }
  }

  @Override
  public void complete (final NodeToken token, final String arcName)
  {
    GraphProcess process = token.getProcess();

    completeNodeExecution( token, arcName, false );

    if ( process.isExecuting() && !arcExecutionStarted )
    {
      executeQueuedArcTokens( process );
    }
  }

  @Override
  public void completeAsynchronous (final NodeToken token, final String arcName)
  {
    completeNodeExecution( token, arcName, true );
  }

  public void completeWithNewTokenSet (final NodeToken token,
                                       final String    arcName,
                                       final String    tokenSetName,
                                       final int       numberOfTokens,
                                       final boolean   asynchronous,
                                       final Env       initialEnv,
                                       final Map<String,List<?>> initialMemberEnv)
  {
    GraphProcess process = token.getProcess();

    if ( !process.isExecuting() || token.isComplete() )
    {
      return;
    }

    completeNodeToken( process, token, arcName );

    List<? extends Arc> outArcs = process.getGraph().getOutputArcs( token.getNode(), arcName );

    if ( !outArcs.isEmpty() )
    {
      TokenSet tokenSet = getFactory().newTokenSet( process, tokenSetName, numberOfTokens );

      if ( initialEnv != null )
      {
        tokenSet.getEnv().importEnv( initialEnv );
      }

      if ( initialMemberEnv != null )
      {
        TokenSetMemberEnv memberEnv = tokenSet.getMemberEnv();
        for ( Map.Entry<String, List<?>> entry : initialMemberEnv.entrySet() )
        {
          memberEnv.setAttribute( entry.getKey(), entry.getValue() );
        }
      }

      for ( int memberIndex = 0; memberIndex < numberOfTokens; memberIndex++ )
      {
        for ( Arc arc : outArcs )
        {
          ArcToken arcToken = generateArcToken( process, arc, token );

          ArcTokenSetMember setMember = getFactory().newArcTokenSetMember( tokenSet, arcToken, memberIndex );
          tokenSet.getActiveArcTokens( this ).add( arcToken );
          arcToken.getTokenSetMemberships().add( setMember );

          finishNewArcTokenProcessing( process, arcToken, asynchronous );
        }
      }
    }
  }

  protected void completeNodeExecution (final NodeToken token,
                                        final String arcName,
                                        final boolean asynchronous)
  {
    GraphProcess process = token.getProcess();

    if ( !process.isExecuting() || token.isComplete() )
    {
      return;
    }

    completeNodeToken( process, token, arcName );

    for ( Arc arc : process.getGraph().getOutputArcs( token.getNode(), arcName ) )
    {
      ArcToken arcToken = generateArcToken( process, arc, token );
      finishNewArcTokenProcessing( process, arcToken, asynchronous );
    }
  }

  private void completeNodeToken (final GraphProcess process,
                                  final NodeToken token,
                                  final String arcName)
  {
    process.removeActiveNodeToken( token );
    token.markComplete();
    NodeTokenEvent.fireCompletedEvent( this, token, arcName );
  }

  private ArcToken generateArcToken (final GraphProcess process,
                                     final Arc arc,
                                     final NodeToken token)
  {
    ArcToken arcToken = getFactory().newArcToken( process, arc, ExecutionType.Forward, token );
    token.getChildTokens().add( arcToken );

    for ( NodeTokenSetMember setMember : token.getTokenSetMemberships() )
    {
      TokenSet tokenSet = setMember.getTokenSet();
      if ( !tokenSet.isComplete() )
      {
        ArcTokenSetMember newSetMember = getFactory().newArcTokenSetMember( tokenSet, arcToken, setMember.getMemberIndex() );
        tokenSet.getActiveArcTokens( this ).add( arcToken );
        arcToken.getTokenSetMemberships().add( newSetMember );
      }
    }

    return arcToken;
  }

  private void finishNewArcTokenProcessing (final GraphProcess process,
                                            final ArcToken arcToken,
                                            final boolean asynchronous)
  {
    ArcTokenEvent.fireCreatedEvent( this, arcToken );

    if ( asynchronous && arcExecutionStarted )
    {
      asyncQueue.add( arcToken );
    }
    else
    {
      process.enqueueArcTokenForExecution( arcToken );
    }
  }

  @Override
  public void executeQueuedArcTokens (final GraphProcess process)
  {
    arcExecutionStarted = true;

    try
    {
      while ( !process.isArcTokenQueueEmpty() )
      {
        executeArc( process, process.dequeueArcTokenForExecution() );
      }
      checkForCompletion( process );
    }
    finally
    {
      arcExecutionStarted = false;
      drainAsyncQueue( process );
    }
  }

  private void drainAsyncQueue (final GraphProcess process)
  {
    while ( !asyncQueue.isEmpty() )
    {
      process.enqueueArcTokenForExecution( asyncQueue.remove( 0 ) );
    }
  }

  private void checkForCompletion (final GraphProcess process)
  {
    if ( process.isExecuting() &&
         !process.hasActiveTokens() &&
         process.isArcTokenQueueEmpty() &&
         asyncQueue.isEmpty() )
    {
      process.setState( ProcessState.PendingCompletion );
      EventActions actions = ProcessEvent.fireCompletedEvent( this, process );
      if ( !actions.isEventTypeIncluded( EventActionType.DELAY_PROCESS_FINALIZE_COMPLETE ) )
      {
        finalizeComplete( process );
      }
    }
  }

  @Override
  public void setupScriptEnv (final ScriptEnv env, final NodeToken token)
  {
    env.addVariable( "engine", this );
    env.addVariable( "token", token );
  }

  @Override
  public void addNodeType (final String type,
                           final Class<? extends Node> nodeClass)
  {
    getFactory().addType(type, nodeClass);
  }

  @Override
  public void addGlobalCustomNodeType (final String type,
                                       final Class<? extends CustomNode> nodeClass)
  {
    getFactory().addGlobalCustomType( type, nodeClass );
  }

  @Override
  public void backtrack (final NodeToken token)
  {
    if ( !token.isComplete() )
    {
      throw new SarasvatiException( "Cannot backtrack to a node token which isn't completed." );
    }

    if ( token.getExecutionType().isBacktracked() )
    {
      throw new SarasvatiException( "Cannot backtrack to a node token which has been backtracked." );
    }

    NodeToken resultToken = null;
    BacktrackTokenVisitor visitor = new BacktrackTokenVisitor( this, token );

    if ( token.getChildTokens().isEmpty() )
    {
      resultToken = visitor.backtrackDeadEnd( token );
    }
    else
    {
      TokenTraversals.traverseChildrenInCreateOrder( token, visitor );
      resultToken = visitor.backtrack();
    }

    executeNode( resultToken.getProcess(), resultToken );

    if ( !arcExecutionStarted )
    {
      executeQueuedArcTokens( token.getProcess() );
    }
  }

  @Override
  public RubricEnv newRubricEnv (final NodeToken token)
  {
    return new DefaultRubricEnv( this, token, DefaultRubricFunctionRepository.getGlobalInstance() );
  }

  @Override
  public GuardResponse evaluateGuard (final NodeToken token,
                                      final String guard)
  {
    if ( guard == null || guard.trim().length() == 0 )
    {
      return GuardResponse.ACCEPT_TOKEN_RESPONSE;
    }

    return (GuardResponse) RubricInterpreter.compile( guard ).eval( newRubricEnv( token ) );
  }

  @Override
  public BaseEngine newEngine (final boolean forNested)
  {
    BaseEngine engine = newEngine();

    if ( forNested )
    {
      engine.parentEngine = this;
    }
    return engine;
  }

  @Override
  public BaseEngine getParentEngine ()
  {
    return parentEngine;
  }

  /**
   * Creates a new engine base on the same parameters as this. For
   * example, if the engine is database backed, it should share
   * the same database engine.
   *
   * @return A new engine
   */
  protected abstract BaseEngine newEngine ();

  // ==========================================================================================
  //             Global Event Queue Methods
  // ==========================================================================================

  @Override
  public void addExecutionListener(final Class<? extends ExecutionListener> listenerClass,
                                   final ExecutionEventType... eventTypes)
  {
    globalEventQueue.addListener( this, listenerClass, eventTypes );
  }

  @Override
  public void removeExecutionListener(final Class<? extends ExecutionListener> listenerClass,
                                      final ExecutionEventType... eventTypes)
  {
    globalEventQueue.removeListener( this, listenerClass, eventTypes );
  }

  @Override
  public void addExecutionListener (final GraphProcess process,
                                    final Class<? extends ExecutionListener> listenerClass,
                                    final ExecutionEventType... eventTypes)
  {
    process.getEventQueue().addListener( this, listenerClass, eventTypes );
  }

  @Override
  public void removeExecutionListener (final GraphProcess process,
                                       final Class<? extends ExecutionListener> listenerClass,
                                       final ExecutionEventType... eventTypes)
  {
    process.getEventQueue().removeListener( this, listenerClass, eventTypes );
  }

  public EventActions fireEvent (final ExecutionEvent event)
  {
    EventActions actions = globalEventQueue.fireEvent( event );
    return actions.compose( event.getProcess().getEventQueue().fireEvent( event ) );
  }
}
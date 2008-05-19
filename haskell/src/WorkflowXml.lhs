
> module WorkflowXml where
> import Text.XML.HaXml.Xml2Haskell
> import Text.XML.HaXml.Parse
> import Text.XML.HaXml.Combinators
> import Text.XML.HaXml.Types
> import Workflow
> import qualified Data.Map as Map

> data XmlNode a =
>     XmlNode {
>         getWfNode :: Node a,
>         getArcs :: [NodeId]
>     }

> getWfNodeId = getNodeId.getWfNode

> data XmlProc a = XmlProc ([Element]->(a, [Element]))

> instance Monad (XmlProc) where
>     return a = XmlProc (\_->(a,[]))
>     XmlProc a >>= f = XmlProc (\elemArray->
>         case (a elemArray) of
>             (currValue, xmlElem) -> case (f currValue) of
>                  XmlProc result -> result xmlElem)

> useDoc (Document _ _ element _ ) = useElem element

> useElem element = XmlProc (\_-> ((), [element]))

> getAttr name = XmlProc (\(x:xs)-> (getElemAttr x name, (x:xs)))

> getChildren = XmlProc (\(x:xs)-> (map (cElemToElem) (children (CElem x)), (x:xs)))

> getElemAttr (Elem _ attrList _ ) name
>    | null attrs = ""
>    | otherwise  = attrVal' (head attrs)
>   where
>     attrs = filter (\(attrName, attrValue) -> attrName == name) attrList
>     attrVal' (_, AttValue atlist) = case (head atlist) of (Left val) -> val

> cElemToElem (CElem element) = element

> unwrapXmlProc (XmlProc a) = case (a []) of (result,_) -> result

> loadWfGraphFromDoc doc elemFuncMap =
>     xmlNodesToWfGraph $ unwrapXmlProc $
>          do useDoc doc
>             children <- getChildren
>             return $ processChildNodes children elemFuncMap Map.empty

> processChildNodes [] _ nodeMap = nodeMap
> processChildNodes (e:rest) elemFuncMap nodeMap = processChildNodes rest elemFuncMap newNodeMap
>     where
>         elemName     = case (e) of (Elem name _ _ ) -> name
>         nodeFunction = elemFuncMap Map.! elemName
>         node         = nodeFunction e
>         newNodeMap   = Map.insert (getWfNodeId node) node nodeMap

The following function deal with converting a map of XmlNode instances to
a WfGraph. Since XmlNode instances only track outgoing nodes, we need to
infer the incoming nodes.

> xmlNodesToWfGraph = graphFromArcs.xmlNodesToNodeArcs

> xmlNodesToNodeArcs nodeMap = map (xmlNodeToNodeArcs nodeMap) (Map.elems nodeMap)

> xmlNodeToNodeArcs nodeMap xmlNode = NodeArcs node inputs outputs
>     where
>         node      = getWfNode xmlNode
>         inputs    = xmlNodeInputs xmlNode nodeMap
>         outputs   = map (toNode) $ getArcs xmlNode
>         mapLookup = (Map.!) nodeMap
>         toNode    = getWfNode.mapLookup

> xmlNodeInputs xmlNode nodeMap = concatMap (matching) nodes
>     where
>         matching = getMatchingOutputs xmlNode nodeMap
>         nodes    = Map.elems nodeMap

> getMatchingOutputs target nodeMap source = map (toNode) matchingNodeIds
>     where
>         mapLookup       = (Map.!) nodeMap
>         toNode          = getWfNode.mapLookup
>         matchingNodeIds = filter ((==) targetNodeId) (getArcs source)
>         targetNodeId    = getWfNodeId target
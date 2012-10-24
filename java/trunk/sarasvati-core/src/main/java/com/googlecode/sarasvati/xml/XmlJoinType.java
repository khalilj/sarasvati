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

    Copyright 2008 Paul Lorenz
*/

package com.googlecode.sarasvati.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.googlecode.sarasvati.JoinType;

@XmlEnum(String.class)
public enum XmlJoinType
{
  @XmlEnumValue ("or")            OR( JoinType.OR ),
  @XmlEnumValue ("and")           AND( JoinType.AND ),
  @XmlEnumValue ("labelAnd")      LABEL_AND( JoinType.LABEL ),
  @XmlEnumValue ("tokenSet")      TOKEN_SET( JoinType.TOKEN_SET ),
  @XmlEnumValue ("tokenSetOr")    TOKEN_SET_OR( JoinType.TOKEN_SET_OR ),
  @XmlEnumValue ("class")         CLASS( JoinType.CLASS ),
  @XmlEnumValue ("first")         FIRST( JoinType.FIRST),
  @XmlEnumValue ("joinlang")      JOINLANG( JoinType.JOINLANG ),
  @XmlEnumValue ("tokenSetAnd")   TOKEN_SET_AND( JoinType.TOKEN_SET_AND ),
  @XmlEnumValue ("tokenSetLabel") TOKEN_SET_LABEL( JoinType.TOKEN_SET_OR );

  private final JoinType joinType;

  private XmlJoinType (final JoinType joinType)
  {
    this.joinType = joinType;
  }

  public JoinType getJoinType ()
  {
    return joinType;
  }

  public static XmlJoinType getXmlJoinType (final JoinType joinType)
  {
    for ( XmlJoinType xmlJoinType : values() )
    {
      if ( joinType == xmlJoinType.getJoinType() )
      {
        return xmlJoinType;
      }
    }

    return null;
  }
}

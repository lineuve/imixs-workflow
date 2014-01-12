/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.plugins;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Plugin;
import org.imixs.workflow.WorkflowContext;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.plugins.jee.MailPlugin;

/**
 * This abstract class implements different helper methods used by subclasses
 * 
 * @author Ralph Soika
 * @version 1.1
 * @see org.imixs.workflow.WorkflowManager
 * 
 */

public abstract class AbstractPlugin implements Plugin {

	public WorkflowContext ctx;

	public void init(WorkflowContext actx) throws PluginException {
		ctx = actx;
	}

	/**
	 * this method parses a string for xml tag <itemvalue>. Those tags will be
	 * replaced with the corresponding item value.
	 * 
	 * <code>
	 *   
	 *   hello <itemvalue>namCreator</itemvalue>
	 *   
	 *   
	 * </code>
	 * 
	 * Item values can also be formated. e.g. for date/time values
	 * 
	 * <code>
	 *  
	 *  Last access Time= <itemvalue format="mm:ss">$created</itemvalue>
	 * 
	 * </code>
	 * 
	 * If the itemValue is a multiValue object the single values can be
	 * spearated by a separator
	 * 
	 * <code>
	 *  
	 *  Phone List: <itemvalue separator="<br />">txtPhones</itemvalue>
	 * 
	 * </code>
	 * 
	 * 
	 * 
	 */
	public String replaceDynamicValues(String aString,
			ItemCollection documentContext) {
		int iTagStartPos;
		int iTagEndPos;

		int iContentStartPos;
		int iContentEndPos;

		int iFormatStartPos;
		int iFormatEndPos;

		int iSeparatorStartPos;
		int iSeparatorEndPos;

		String sFormat = "";
		String sSeparator = " ";
		String sItemValue;

		if (aString == null)
			return "";

		// test if a <value> tag exists...
		while ((iTagStartPos = aString.toLowerCase().indexOf("<itemvalue")) != -1) {

			iTagEndPos = aString.toLowerCase().indexOf("</itemvalue>",
					iTagStartPos);

			// if no end tag found return string unchanged...
			if (iTagEndPos == -1)
				return aString;

			// reset pos vars
			iContentStartPos = 0;
			iContentEndPos = 0;
			iFormatStartPos = 0;
			iFormatEndPos = 0;
			iSeparatorStartPos = 0;
			iSeparatorEndPos = 0;
			sFormat = "";
			sSeparator = " ";
			sItemValue = "";

			// so we now search the beginning of the tag content
			iContentEndPos = iTagEndPos;
			// start pos is the last > before the iContentEndPos
			String sTestString = aString.substring(0, iContentEndPos);
			iContentStartPos = sTestString.lastIndexOf('>') + 1;

			// if no end tag found return string unchanged...
			if (iContentStartPos >= iContentEndPos)
				return aString;

			iTagEndPos = iTagEndPos + "</itemvalue>".length();

			// now we have the start and end position of a tag and also the
			// start and end pos of the value

			// next we check if the start tag contains a 'format' attribute
			iFormatStartPos = aString.toLowerCase().indexOf("format=",
					iTagStartPos);
			// extract format string if available
			if (iFormatStartPos > -1 && iFormatStartPos < iContentStartPos) {
				iFormatStartPos = aString.indexOf("\"", iFormatStartPos) + 1;
				iFormatEndPos = aString.indexOf("\"", iFormatStartPos + 1);
				sFormat = aString.substring(iFormatStartPos, iFormatEndPos);
			}

			// next we check if the start tag contains a 'separator'
			// attribute
			iSeparatorStartPos = aString.toLowerCase().indexOf("separator=",
					iTagStartPos);
			// extract format string if available
			if (iSeparatorStartPos > -1
					&& iSeparatorStartPos < iContentStartPos) {
				iSeparatorStartPos = aString.indexOf("\"", iSeparatorStartPos) + 1;
				iSeparatorEndPos = aString
						.indexOf("\"", iSeparatorStartPos + 1);
				sSeparator = aString.substring(iSeparatorStartPos,
						iSeparatorEndPos);
			}

			// extract Item Value
			sItemValue = aString.substring(iContentStartPos, iContentEndPos);

			// format field value
			List vValue = documentContext.getItemValue(sItemValue);

			String sResult = formatItemValues(vValue, sSeparator, sFormat);

			// now replace the tag with the result string
			aString = aString.substring(0, iTagStartPos) + sResult
					+ aString.substring(iTagEndPos);
		}

		return aString;

	}

	/**
	 * this method formats a string object depending of an attribute type.
	 * MultiValues will be separated by the provided separator
	 */
	public String formatItemValues(Collection aItem, String aSeparator,
			String sFormat) {

		StringBuffer sBuffer = new StringBuffer();

		if (aItem == null)
			return "";

		for (Object aSingleValue : aItem) {
			String aValue = formatObjectValue(aSingleValue, sFormat);
			sBuffer.append(aValue);
			// append delimiter
			sBuffer.append(aSeparator);
		}

		String sString = sBuffer.toString();

		// cut last separator
		if (sString.endsWith(aSeparator)) {
			sString = sString.substring(0, sString.lastIndexOf(aSeparator));
		}

		return sString;

	}

	/**
	 * This helper method test the type of an object provided by a
	 * itemcollection and formats the object into a string value.
	 * 
	 * Only Date Objects will be formated into a modified representation. other
	 * objects will be returned using the toString() method.
	 * 
	 * if an optional format is provided this will be used to format date
	 * objects.
	 * 
	 * @param o
	 * @return
	 */
	private String formatObjectValue(Object o, String format) {

		Date dateValue = null;

		// now test the objct type to date
		if (o instanceof Date) {
			dateValue = (Date) o;
		}

		if (o instanceof Calendar) {
			Calendar cal = (Calendar) o;
			dateValue = cal.getTime();
		}

		// format date string?
		if (dateValue != null) {
			String singleValue = "";
			if (format != null && !"".equals(format)) {
				// format date with provided formater
				try {
					SimpleDateFormat formatter = new SimpleDateFormat(format);
					singleValue = formatter.format(dateValue);
				} catch (Exception ef) {
					Logger logger = Logger.getLogger(AbstractPlugin.class.getName());
					logger.warning("AbstractPlugin: Invalid format String '" + format+"'");
					logger.warning("AbstractPlugin: Can not format value - error: " + ef.getMessage());
					return ""+dateValue;
				}
			} else
				// use standard formate short/short
				singleValue = DateFormat.getDateTimeInstance(DateFormat.SHORT,
						DateFormat.SHORT).format(dateValue);

			return singleValue;
		}

		return o.toString();
	}

	/**
	 * This method merges the values from p_VectorSource into vectorDest and
	 * removes duplicates
	 * 
	 * @param p_VectorDestination
	 * @param p_VectorSource
	 */
	public void mergeVectors(List p_VectorDestination, List p_VectorSource) {
		if ((p_VectorSource != null) && (p_VectorSource.size() > 0)) {
			for (Object o : p_VectorSource) {
				if (p_VectorDestination.indexOf(o) == -1)
					p_VectorDestination.add(o);
			}
		}
	}

	/**
	 * this method merges the values of p_VectorFieldList into
	 * p_VectorDestination and test for duplicates
	 * 
	 * @param p_VectorDestination
	 * @param p_VectorFieldList
	 */
	public void mergeMappedFieldValues(ItemCollection documentContext,
			List p_VectorDestination, List<String> p_VectorFieldList) {
		if (p_VectorDestination == null || p_VectorFieldList == null)
			return;

		if (p_VectorFieldList.size() > 0) {
			// iterate over fieldlist
			for (String sFeldName : p_VectorFieldList) {
				List vValues = documentContext.getItemValue(sFeldName);
				// now append the values into p_VectorDestination
				if ((vValues != null) && (vValues.size() > 0)) {
					for (Object o : vValues) {
						// append only if not used
						if (p_VectorDestination.indexOf(o) == -1)
							p_VectorDestination.add(o);
					}
				}
			}
		}

	}

	/**
	 * this method removes duplicate and null values from a vector object The
	 * method is called by the run method after build new read and write access
	 * elements.
	 * 
	 * @param p_Vector
	 */
	public List uniqueList(List p_Vector) {
		int iVectorSize = p_Vector.size();
		Vector cleanedVector = new Vector();

		for (int i = 0; i < iVectorSize; i++) {
			Object o = p_Vector.get(i);
			if (o == null || cleanedVector.indexOf(o) > -1
					|| "".equals(o.toString()))
				continue;

			// add unique object
			cleanedVector.add(o);
		}
		p_Vector = cleanedVector;
		// do not work with empty vectors....
		if (p_Vector.size() == 0)
			p_Vector.add("");

		return p_Vector;
	}
}
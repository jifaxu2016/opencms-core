/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsAttributeComparisonList.java,v $
 * Date   : $Date: 2005/12/14 09:52:45 $
 * Version: $Revision: 1.1.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.comparison;

import org.opencms.file.CmsBackupResourceHandler;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * List for property comparison including columns for property name and the values. <p>
 * 
 * @author Jan Baudisch  
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAttributeComparisonList extends CmsPropertyComparisonList {

    /** List id constant. */
    public static final String LIST_ID = "hiacl";

    private static boolean m_showAllAttributes = false;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAttributeComparisonList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAttributeComparisonList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * 
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsAttributeComparisonList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_COMPARE_ATTRIBUTES_0),
            LIST_COLUMN_PROPERTY_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * 
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListIndepActions()
     */
    public void executeListIndepActions() {

        m_showAllAttributes = !m_showAllAttributes;
        refreshList();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        // forward to the edit module screen  
        Map params = new HashMap();
        params.put(CmsHistoryList.PARAM_TAGID_1, getParamTagId1());
        params.put(CmsHistoryList.PARAM_TAGID_2, getParamTagId2());
        params.put(CmsHistoryList.PARAM_VERSION_1, getParamVersion1());
        params.put(CmsHistoryList.PARAM_VERSION_2, getParamVersion2());
        params.put(CmsHistoryList.PARAM_PATH_1, getParamPath1());
        params.put(CmsHistoryList.PARAM_PATH_2, getParamPath2());
        params.put(PARAM_COMPARE, CmsResourceComparisonDialog.COMPARE_ATTRIBUTES);
        params.put(PARAM_RESOURCE, getParamResource());
        // forward to the difference screen
        getToolManager().jspForwardTool(this, "/history/comparison/difference", params);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        Iterator diffs = CmsResourceComparison.compareAttributes(getCms(), getFile1(), getFile2()).iterator();
        while (diffs.hasNext()) {
            CmsAttributeComparison comparison = (CmsAttributeComparison)diffs.next();
            CmsListItem item = getList().newItem(comparison.getName());
            item.set(LIST_COLUMN_PROPERTY_NAME, key(comparison.getName()));
            item.set(LIST_COLUMN_VERSION_1, CmsStringUtil.trimToSize(comparison.getVersion1(), TRIM_AT_LENGTH));
            item.set(LIST_COLUMN_VERSION_2, CmsStringUtil.trimToSize(comparison.getVersion2(), TRIM_AT_LENGTH));
            if (CmsResourceComparison.TYPE_ADDED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_ADDED_0));
            } else if (CmsResourceComparison.TYPE_REMOVED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_REMOVED_0));
            } else if (CmsResourceComparison.TYPE_CHANGED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_CHANGED_0));
            } else {
                if (!m_showAllAttributes) {
                    // do not display entry
                    continue;
                } else {
                    item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_UNCHANGED_0));
                }
            }
            ret.add(item);
        }
        return ret;
    }

    /**
     * Returns the html code to display a file version.<p>
     * 
     * @param path the path of the file to be displayed
     * @param tagId the version id of the file to be displayed
     * @return the html code to display a file version
     */
    protected String getViewVersionButtonHtml(String path, String tagId) {

        String label = Messages.get().container(Messages.GUI_COMPARE_VIEW_VERSION_1, getParamVersion2()).key(
            getLocale());
        String iconPath = null;
        try {
            String typeName = OpenCms.getResourceManager().getResourceType(getFile1().getTypeId()).getTypeName();
            iconPath = "filetypes/" + OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName).getIcon();
        } catch (CmsException e) {
            iconPath = "filetypes/" + OpenCms.getWorkplaceManager().getExplorerTypeSetting("plain").getIcon();
        }
        StringBuffer result = new StringBuffer(1024);
        result.append("<span class='link' onClick='");
        result.append("window.open('");
        StringBuffer link = new StringBuffer(1024);
        if ("-1".equals(tagId)) {
            // offline version
            link.append(getParamResource());
        } else {
            // backup version
            link.append(CmsBackupResourceHandler.BACKUP_HANDLER);
            link.append(path);
            link.append('?');
            link.append(CmsBackupResourceHandler.PARAM_VERSIONID);
            link.append('=');
            link.append(tagId);
        }
        result.append(getJsp().link(link.toString()));
        result.append("','version','scrollbars=yes, resizable=yes, width=800, height=600')");
        result.append("'><img style='width: 16px; height: 16px;' src='");
        result.append(CmsWorkplace.getSkinUri());
        result.append(iconPath);
        result.append("' alt='");
        result.append(label);
        result.append("' title='");
        result.append(label);
        result.append("'>&nbsp;<a href='#'>");
        result.append(label);
        result.append("</a></span>");

        return result.toString();
    }

    /**
     * 
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        addMessages(org.opencms.workplace.commons.Messages.get().getBundleName());
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add the delete module multi action
        CmsListIndependentAction viewVersion1 = new CmsListIndependentAction(LIST_ACTION_VIEW1) {

            public String buttonHtml(CmsWorkplace wp) {

                return getViewVersionButtonHtml(getParamPath1(), getParamTagId1());
            }
        };
        metadata.addIndependentAction(viewVersion1);
        // add the delete module multi action
        CmsListIndependentAction viewVersion2 = new CmsListIndependentAction(LIST_ACTION_VIEW2) {

            public String buttonHtml(CmsWorkplace wp) {

                return getViewVersionButtonHtml(getParamPath2(), getParamTagId2());
            }
        };
        metadata.addIndependentAction(viewVersion2);

        // add event details
        CmsListItemDetails eventDetails = new CmsListItemDetails(LIST_IACTION_SHOW);
        if (m_showAllAttributes) {
            eventDetails.setVisible(true);
        } else {
            eventDetails.setVisible(false);
        }
        eventDetails.setShowActionName(Messages.get().container(Messages.GUI_COMPARE_SHOW_ALL_ATTRIBUTES_0));
        eventDetails.setHideActionName(Messages.get().container(Messages.GUI_COMPARE_HIDE_IDENTICAL_ATTRIBUTES_0));
        metadata.addItemDetails(eventDetails);
    }
}
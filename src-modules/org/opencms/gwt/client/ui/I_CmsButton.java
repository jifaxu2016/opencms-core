/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/I_CmsButton.java,v $
 * Date   : $Date: 2011/03/14 18:31:47 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;

/**
 * Interface to hold button related enumerations. To be used with {@link org.opencms.gwt.client.ui.CmsPushButton}
 * and {@link org.opencms.gwt.client.ui.CmsToggleButton}.<p>
 */
public interface I_CmsButton {

    /** CSS style variants. */
    public static enum Size {

        /** Big button style. */
        big(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonBig()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textBig()),

        /** Medium button style. */
        medium(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonMedium()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textMedium()),

        /** Small button style. */
        small(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonSmall()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textSmall());

        /** The CSS class name. */
        private String m_cssClassName;

        /**
         * Constructor.<p>
         * 
         * @param cssClassName the CSS class name
         */
        Size(String cssClassName) {

            m_cssClassName = cssClassName;
        }

        /**
         * Returns the CSS class name of this style.<p>
         * 
         * @return the CSS class name
         */
        public String getCssClassName() {

            return m_cssClassName;
        }
    }

    /** Available button icons. */
    public enum ButtonData {

        /** Toolbar button. */
        ADD(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarAdd(), Messages.get().key(
            Messages.GUI_TOOLBAR_ADD_0)),

        /** Toolbar button. */
        ADD_TO_FAVORITES(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarClipboard(),
        Messages.get().key(Messages.GUI_TOOLBAR_ADD_TO_FAVORITES_0)),

        /** Toolbar button. */
        CONTEXT(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarContext(), Messages.get().key(
            Messages.GUI_TOOLBAR_CONTEXT_0)),

        /** Toolbar button. */
        CLIPBOARD(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarClipboard(), Messages.get().key(
            Messages.GUI_TOOLBAR_CLIPBOARD_0)),

        /** Toolbar button. */
        EDIT(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarEdit(), Messages.get().key(
            Messages.GUI_TOOLBAR_EDIT_0)),

        /** Toolbar button. */
        MOVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarMove(), Messages.get().key(
            Messages.GUI_TOOLBAR_MOVE_0)),

        /** Toolbar button. */
        NEW(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarNew(), Messages.get().key(
            Messages.GUI_TOOLBAR_NEW_0)),

        /** Toolbar button. */
        PROPERTIES(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarProperties(), Messages.get().key(
            Messages.GUI_TOOLBAR_PROPERTIES_0)),

        /** Toolbar button. */
        PUBLISH(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarPublish(), Messages.get().key(
            Messages.GUI_TOOLBAR_PUBLISH_0)),

        /** Toolbar button. */
        REMOVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarRemove(), Messages.get().key(
            Messages.GUI_TOOLBAR_REMOVE_0)),

        /** Toolbar button. */
        RESET(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarReset(), Messages.get().key(
            Messages.GUI_TOOLBAR_RESET_0)),

        /** Toolbar button. */
        SAVE(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSave(), Messages.get().key(
            Messages.GUI_TOOLBAR_SAVE_0)),

        /** Toolbar button. */
        SELECTION(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSelection(), Messages.get().key(
            Messages.GUI_TOOLBAR_SELECTION_0)),

        /** Toolbar button. */
        SITEMAP(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSitemap(), Messages.get().key(
            Messages.GUI_TOOLBAR_SITEMAP_0));

        /** The icon class name. */
        private String m_iconClass;

        /** The title. */
        private String m_title;

        /**
         * Constructor.<p>
         * 
         * @param iconClass the icon class name
         * @param title the title
         */
        private ButtonData(String iconClass, String title) {

            m_iconClass = iconClass;
            m_title = title;
        }

        /**
         * Returns the CSS class name.<p>
         * 
         * @return the CSS class name
         */
        public String getIconClass() {

            return m_iconClass;
        }

        /**
         * Returns the title.<p>
         * 
         * @return the title
         */
        public String getTitle() {

            return m_title;
        }
    }
}

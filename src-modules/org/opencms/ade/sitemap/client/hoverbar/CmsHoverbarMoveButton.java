/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsHoverbarMoveButton.java,v $
 * Date   : $Date: 2011/02/03 15:13:15 $
 * Version: $Revision: 1.9 $
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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.ui.CmsPushButton;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Sitemap hoverbar move button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 8.0.0
 */
public class CmsHoverbarMoveButton extends CmsPushButton implements I_CmsDragHandle {

    /** The mouse down handler registration. */
    protected HandlerRegistration m_mouseDownHandlerReg;

    /** The current site path. */
    protected String m_sitePath;

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsHoverbarMoveButton(final CmsSitemapHoverbar hoverbar) {

        addStyleName(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarMove());
        setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarMove());
        setTitle(Messages.get().key(Messages.GUI_HOVERBAR_MOVE_0));
        setShowBorder(false);
        hoverbar.addShowHandler(new I_CmsHoverbarShowHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.hoverbar.I_CmsHoverbarShowHandler#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
             */
            public void onShow(CmsHoverbarShowEvent event) {

                m_sitePath = hoverbar.getSitePath();
                final CmsSitemapController controller = hoverbar.getController();
                CmsClientSitemapEntry entry = controller.getEntry(m_sitePath);
                if (CmsSitemapView.getInstance().isNavigationMode() && (entry != null)) {

                    if (!entry.isInNavigation()) {
                        CmsHoverbarMoveButton.this.setVisible(false);
                    } else if (controller.isRoot(m_sitePath)) {
                        disable(Messages.get().key(Messages.GUI_DISABLED_ROOT_ITEM_0));
                        CmsHoverbarMoveButton.this.setVisible(true);
                    } else if (entry.hasForeignFolderLock()) {
                        disable("The entry folder is locked.");
                        CmsHoverbarMoveButton.this.setVisible(true);
                    } else {
                        enable();
                        m_mouseDownHandlerReg = addMouseDownHandler(CmsSitemapView.getInstance().getTree().getDnDHandler());
                        CmsHoverbarMoveButton.this.setVisible(true);
                    }
                } else {
                    CmsHoverbarMoveButton.this.setVisible(false);
                }
            }
        });
        hoverbar.addHideHandler(new I_CmsHoverbarHideHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.hoverbar.I_CmsHoverbarHideHandler#onHide(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarHideEvent)
             */
            public void onHide(CmsHoverbarHideEvent event) {

                if (m_mouseDownHandlerReg != null) {
                    m_mouseDownHandlerReg.removeHandler();
                    m_mouseDownHandlerReg = null;
                }
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDragHandle#getDraggable()
     */
    public I_CmsDraggable getDraggable() {

        return CmsSitemapView.getInstance().getTreeItem(m_sitePath);
    }
}

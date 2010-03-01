/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/Attic/CmsAdvancedGalleryWidgetConfiguration.java,v $
 * Date   : $Date: 2010/03/01 14:21:41 $
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.galleries.CmsGallerySearchServer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Configuration options for the advanced gallery widget (e.g. AdvancedGalleryWidget).<p>
 * 
 * The configuration options are read from the configuration String of the widget. 
 * For nested XML schemas the configuration String must be defined inside the nested content.<p>
 * 
 * The configuration String has to be formatted as JSON object with following structure:
 * <code>{ "gallerykey" : { "optional configuration" } }</code>
 * 
 * The "gallerykey" is the  type of the gallery. It defines the resource types and associated gallery types, which will be displayed via widget. 
 * The following "gallerykey"s are possible:
 * <ul>
 * <li><code>imagegallery</code></li>
 * <li><code>downloadgallery</code></li>
 * <li><code>container</code></li>
 * <li><code>sitemap</code></li>
 * </ul>
 * If no "gallerykey" is given, a default configuration for the widget will be used. In this case no resource types are specified.  
 * 
 * Following keys can be used for all gallery types("gallerykey") inside the "optional configuration" of the widget:
 * <ul>
 * <li><code>class</code>: optional class implementing {@link I_CmsGalleryWidgetDynamicConfiguration} to dynamically
 *            configure startup parameters and format values.</li>
 * <li><code>startup</code>: the startup folder('/demo_t3/documents/') or the startup folders(['/demo_t3/documents/','/demo_t3/test/']), can be dynamically generated by the provided class,
 *            in that case, use 'dynamic' as value.</li>
 * <li><code>type</code>: the startup folder type, can be 'gallery' or 'category'. Can be dynamically generated
 *            by the provided class, in that case, use 'dynamic' as value.</li>
 * </ul>
 * 
 * The following additional keys are possible for "imagegallery":
 * <ul>
 * <li><code>class</code>: optional class implementing {@link I_CmsImageWidgetDynamicConfiguration} to dynamically
 *            configure startup parameters and format values.</li>
 * <li><code>formatnames</code>: list of format names to select, with pairs of selectable value and selectable text,
 *            e.g. value1:optiontext1|value2:optiontext2</li>
 * <li><code>formatvalues</code>: corresponding format values to the format names list,
 *            can be dynamically generated by the dynamic configuration class.
 *            The list of values should contain width and height information, with a '?' as sign for dynamic size
 *            and with an 'x' as separator for width and height.
 *            Example: ['200x?', '800x600']</li>
 * <li><code>scaleparams</code>: default scale parameters (no width, height or crop information should be provided!)</li>
 * <li><code>useformat</code>: indicates if the format select box for the image should be shown or not.</li>
 * </ul>
 * 
 * Example configurations can look like this:<p>
 * <code>{imagegallery:{startup:['species/composite-plants/','species/'],type:'category', useformat:false}}</code><p>
 * <code>{imagegallery:{startup:/demo_t3/images/,type:'gallery', useformat:true}}</code><p>
 * <code>{downloadgallery:{startup:['/demo_t3/documents/'],type:'gallery'}}</code><p>
 * <code>{downloadgallery:{}}</code><p>
 *  <code>{sitemap:{}}</code><p>
 *
 * @author Polina Smagina
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 
 */
public class CmsAdvancedGalleryWidgetConfiguration {

    /** Gallery keys for different gallery types. */
    public enum CmsGalleryConfigKeys {
        /** Configuration key name for the container pages. */
        container,

        /** Configuration key name for the default configuration. */
        defaultgallery,

        /** Configuration key name for the downloadgallery. */
        downloadgallery,

        /** Configuration key name for the imagegallery. */
        imagegallery,

        /** Configuration key name for the sitemap. */
        sitemap;

        /** The default configuration gallery key. */
        public static final CmsGalleryConfigKeys DEFAULT = defaultgallery;
    }

    /** Configuration key names constants. */
    public enum CmsGalleryConfigParam {

        /** Configuration value name for a dynamic configuration. */
        CONFIG_KEY_BUTTON("button"),

        /** Configuration key name for the class configuration. */
        CONFIG_KEY_CLASS("class"),

        /** Configuration key name for the formatnames configuration. */
        CONFIG_KEY_FORMATNAMES("formatnames"),

        /** Configuration key name for the formatvalues configuration. */
        CONFIG_KEY_FORMATVALUES("formatvalues"),

        /** Configuration key name for the configuration of the resource types. */
        CONFIG_KEY_RESOURCETYPES("resourcetypes"),

        /** Configuration key name for the scaleparams configuration. */
        CONFIG_KEY_SCALEPARAMS("scaleparams"),

        /** Configuration key name for the startup configuration. */
        CONFIG_KEY_STARTUP("startup"),

        /** Configuration key name for the type configuration. */
        CONFIG_KEY_TYPE("type"),

        /** Configuration key name for the usedescription configuration. */
        CONFIG_KEY_USEDESCRIPTION("usedescription"),

        /** Configuration key name for the useformat configuration. */
        CONFIG_KEY_USEFORMAT("useformat"),

        /** Configuration value name for a dynamic configuration. */
        CONFIG_VALUE_DYNAMIC("dynamic");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private CmsGalleryConfigParam(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** Configuration values. */
    public enum CmsGalleryConfigValues {

        /** The button prefix for the containerpagegallery. */
        CONTAINER("html"),

        /** The button prefix for the default gallery. */
        DEFAULT("html"),

        /** The button prefix for the downloadgallery. */
        DOWNLOAD("download"),

        /** The button prefix for the imagegallery. */
        IMAGE("image"),

        /** The resource type array for all available resources. */
        RESOURCETYPE_DEFAULTGALLERY("[]"),

        /** The button prefix for the sitemapgallery. */
        SITEMAP("html"),

        /** Tabs configuration for the containerpagegallery. */
        TABS_CONTAINERPAGEGALLERY("['cms_tab_types','cms_tab_galleries','cms_tab_categories','cms_tab_search']"),

        /** Tabs configuration for the containerpagegallery. */
        TABS_DEFAULTGALLERY("['cms_tab_galleries','cms_tab_categories','cms_tab_search']"),

        /** Tabs configuration for the downloadgallery. */
        TABS_DOWNLOADGALLERY("['cms_tab_galleries','cms_tab_categories','cms_tab_search']"),

        /** Tabs configuration for the imagegallery. */
        TABS_IMAGEGALLERY("['cms_tab_galleries','cms_tab_categories','cms_tab_search']"),

        /** Tabs configuration for the sitemapgallery. */
        TABS_SITEMAP("['cms_tab_categories','cms_tab_search','cms_tab_sitemap']");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private CmsGalleryConfigValues(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsAdvancedGalleryWidgetConfiguration.class);

    /** The prefix for the button for the advanced gallery. */
    protected String m_buttonPrefix;

    /** The optional class name for generating dynamic configurations, must implement {@link I_CmsGalleryWidgetDynamicConfiguration}. */
    protected String m_className;

    /** The list of image format values matching the options for the format select box. */
    protected List<String> m_formatValues;

    /** Flag to indicate imagegallery configuration. */
    protected boolean m_isImagegallery;

    /** The resource types to be load in this advanced gallery widget. */
    protected JSONArray m_resourceTypes;

    /** The scale parameters to apply to a scaled image (e.g. quality, type). */
    protected String m_scaleParams;

    /** The list of select options for the format select box, must contain {@link CmsSelectWidgetOption} objects. */
    protected List<String> m_selectFormat;

    /** The select options for the format select box as String. */
    protected String m_selectFormatString;

    /** The flag if the format select box should be shown. */
    protected boolean m_showFormat;

    /** The required information for the initial item list to load. */
    protected String m_startup;

    /** The initial loaded folders. */
    protected JSONArray m_startUpFolders;

    /** The html id of the tab to open by gallery start. */
    protected String m_startupTabId;

    /** The tabs to be display for the gallery. */
    protected JSONArray m_tabs;

    /** The type of the initial item list to load, either gallery or category. */
    protected String m_type;

    /**
     * Default constructor.<p>
     */
    public CmsAdvancedGalleryWidgetConfiguration() {

        // empty constructor is required for class registration       
    }

    /**
     * Generates an initialized configuration for the advanced gallery item widget using the given configuration string.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @param configuration the widget configuration string
     */
    public CmsAdvancedGalleryWidgetConfiguration(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param,
        String configuration) {

        init(cms, widgetDialog, param, configuration);

    }

    /**
     * Returns the prefix for the advanced gallery, depending on the gallery type.<p>
     *
     * @return the prefix for the gallery button
     */
    public String getButtonPrefix() {

        return m_buttonPrefix;
    }

    /**
     * Returns the optional class name for generating dynamic configurations, must implement {@link I_CmsGalleryWidgetDynamicConfiguration}.<p>
     * 
     * @return the optional class name for generating dynamic configurations
     */
    public String getClassName() {

        return m_className;
    }

    /**
     * Returns the list of image format values matching the options for the format select box.<p>
     * 
     * @return the list of image format values matching the options for the format select box
     */
    public List getFormatValues() {

        return m_formatValues;
    }

    /**
     * Returns the resourceTypes.<p>
     *
     * @return the resourceTypes
     */
    public JSONArray getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Returns the scale parameters to apply to a scaled image (e.g. quality, type).<p>
     * 
     * @return scale the parameters to apply to a scaled image
     */
    public String getScaleParams() {

        return m_scaleParams;
    }

    /**
     * Returns the list of select options for the format select box, must contain {@link CmsSelectWidgetOption} objects.<p>
     * 
     * @return the list of select options for the format select box
     */
    public List getSelectFormat() {

        return m_selectFormat;
    }

    /**
     * Returns the select options for the format select box as String.<p>
     * 
     * The String has the following structure <code>format name 1:localized name 1|format name 2:localized name 2|...</code>.<p>
     * 
     * @return the select options for the format select box
     */
    public String getSelectFormatString() {

        return m_selectFormatString;
    }

    /**
     * Returns the required information for the initial item list to load.<p>
     * 
     * If a gallery should be shown, the path to the gallery must be specified,
     * for a category the category path (e.g wurstsorten/kochwurst/).<p>
     * 
     * @return the required information for the initial item list to load
     */
    public String getStartup() {

        return m_startup;
    }

    /**
     * Returns the startUpFolders.<p>
     *
     * @return the startUpFolders
     */
    public JSONArray getStartUpFolders() {

        return m_startUpFolders;
    }

    /**
     * Returns the html tab id to start the gallery.<p>
     *
     * @return the thtml tab id
     */
    public String getStartupTabId() {

        return m_startupTabId;
    }

    /**
     * Returns the tabs to be displayed in the gallery.<p>
     *
     * @return the tabs as JSON array
     */
    public JSONArray getTabs() {

        return m_tabs;
    }

    /**
     * Returns the type of the initial item list to load, either gallery or category.<p>
     * 
     * @return the type of the initial image list to load
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the imagegallery flag. <p>
     *
     * @return true, if gallery is of type imagegallery, false otherwise
     */
    public boolean isImagegallery() {

        return m_isImagegallery;
    }

    /**
     * Returns if the format select box should be shown.<p>
     * 
     * @return true if the format select box should be shown, otherwise false
     */
    public boolean isShowFormat() {

        return m_showFormat;
    }

    /**
     * Sets the tab id to start the gallery by opening.<p>
     *
     * @param startupTabId the start tab id
     */
    public void setStartupTabId(String startupTabId) {

        m_startupTabId = startupTabId;
    }

    /**
     * Sets the type of the initial item list to load, either gallery or category.<p>
     * 
     * @param type the type of the initial item list to load
     */
    protected void setType(String type) {

        m_type = type;
    }

    /**
     * Returns the gallery type name from the configuration. <p>
     * 
     * @param jsonObj the configuration of the gallery as json object 
     * @return the gallery name key name
     */
    protected CmsGalleryConfigKeys getGalleryType(JSONObject jsonObj) {

        if (jsonObj.has(CmsGalleryConfigKeys.downloadgallery.toString())) {
            return CmsGalleryConfigKeys.downloadgallery;
        } else if (jsonObj.has(CmsGalleryConfigKeys.imagegallery.toString())) {
            return CmsGalleryConfigKeys.imagegallery;
        } else if (jsonObj.has(CmsGalleryConfigKeys.container.toString())) {
            return CmsGalleryConfigKeys.container;
        } else if (jsonObj.has(CmsGalleryConfigKeys.sitemap.toString())) {
            return CmsGalleryConfigKeys.sitemap;
        } else {
            return CmsGalleryConfigKeys.DEFAULT;
        }
    }

    /**
     * Initializes the widget configuration using the given configuration string.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @param configuration the widget configuration string
     */
    protected void init(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param, String configuration) {

        // if configuration is set, generate JSON object
        JSONObject configJsonObj = new JSONObject();
        if (configuration != null) {
            configuration = CmsMacroResolver.resolveMacros(configuration, cms, widgetDialog.getMessages());
            try {
                configJsonObj = new JSONObject(configuration);
            } catch (JSONException e) {
                // initialization failed
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        // handle the given configuration 
        // or set default configuration for downloadgallery         
        JSONObject gConfigJsonObj = new JSONObject();
        JSONArray resTypes = new JSONArray();
        JSONArray tabs = new JSONArray();
        switch (getGalleryType(configJsonObj)) {
            case imagegallery:
                // set the preselected resource types available for this advanced gallery
                // set the preselected gallery tabs to be displayed
                resTypes = new JSONArray();
                try {
                    resTypes.put(OpenCms.getResourceManager().getResourceType(CmsResourceTypeImage.getStaticTypeName()).getTypeId());
                    tabs = new JSONArray(CmsGalleryConfigValues.TABS_IMAGEGALLERY.getName());
                } catch (CmsLoaderException e) {
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                } catch (JSONException e) {
                    // should not happen
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                setResourceTypes(resTypes);
                setResourceTypes(resTypes);
                setTabs(tabs);
                setImagegallery(true);
                // set the prefix for the imagegallery button
                setButtonPrefix(CmsGalleryConfigValues.IMAGE.getName());
                // set start tab
                setStartupTabId(CmsGallerySearchServer.TabId.cms_tab_galleries.toString());

                prepareAndSetImageGalleryConfigParams(cms, widgetDialog, param, configJsonObj);
                break;
            case sitemap:
                // set the preselected resource types available for this advanced gallery
                // set the preselected gallery tabs to be displayed
                try {
                    resTypes.put(OpenCms.getResourceManager().getResourceType(
                        CmsResourceTypeXmlContainerPage.getStaticTypeName()).getTypeId());
                    tabs = new JSONArray(CmsGalleryConfigValues.TABS_SITEMAP.getName());
                } catch (CmsLoaderException e) {
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                } catch (JSONException e) {
                    // should not happen
                    resTypes = new JSONArray();
                    tabs = new JSONArray();
                }
                setResourceTypes(resTypes);
                setTabs(tabs);
                setImagegallery(false);
                // set the prefix for the containerpage button
                setButtonPrefix(CmsGalleryConfigValues.SITEMAP.getName());
                // set start tab
                setStartupTabId(CmsGallerySearchServer.TabId.cms_tab_sitemap.toString());

                // set the parameter from the configuration
                gConfigJsonObj = configJsonObj.optJSONObject(CmsGalleryConfigKeys.sitemap.toString());
                setDefaultgalleryConfigPart(cms, widgetDialog, param, gConfigJsonObj);
                break;
            case container:
                // set the preselected resource types available for this advanced gallery
                // set the preselected gallery tabs to be displayed
                try {
                    resTypes.put(OpenCms.getResourceManager().getResourceType(
                        CmsResourceTypeXmlContainerPage.getStaticTypeName()).getTypeId());
                    tabs = new JSONArray(CmsGalleryConfigValues.TABS_CONTAINERPAGEGALLERY.getName());
                } catch (CmsLoaderException e) {
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                } catch (JSONException e) {
                    // should not happen
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                setResourceTypes(resTypes);
                setTabs(tabs);
                setImagegallery(false);
                // set the prefix for the sitemap button
                setButtonPrefix(CmsGalleryConfigValues.CONTAINER.getName());
                // set start tab
                setStartupTabId(CmsGallerySearchServer.TabId.cms_tab_results.toString());

                // set the parameter from the configuration
                gConfigJsonObj = configJsonObj.optJSONObject(CmsGalleryConfigKeys.container.toString());
                setDefaultgalleryConfigPart(cms, widgetDialog, param, gConfigJsonObj);
                break;
            case downloadgallery:
                // set the preselected resource types available for this advanced gallery
                // set the preselected gallery tabs to be displayed
                try {
                    resTypes.put(OpenCms.getResourceManager().getResourceType(CmsResourceTypeBinary.getStaticTypeName()).getTypeId());
                    tabs = new JSONArray(CmsGalleryConfigValues.TABS_DOWNLOADGALLERY.getName());
                } catch (JSONException e) {
                    // should not happen
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                } catch (CmsLoaderException e) {
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                setResourceTypes(resTypes);
                setTabs(tabs);
                setImagegallery(false);
                // set the prefix for the download button
                setButtonPrefix(CmsGalleryConfigValues.DOWNLOAD.getName());
                // set start tab
                setStartupTabId(CmsGallerySearchServer.TabId.cms_tab_galleries.toString());

                // set the parameter from the configuration
                gConfigJsonObj = configJsonObj.optJSONObject(CmsGalleryConfigKeys.downloadgallery.toString());
                setDefaultgalleryConfigPart(cms, widgetDialog, param, gConfigJsonObj);
                break;
            case defaultgallery:
            default:
                // set the preselected resource types available for this advanced gallery
                // set the preselected gallery tabs to be displayed
                try {
                    resTypes = new JSONArray(CmsGalleryConfigValues.RESOURCETYPE_DEFAULTGALLERY.getName());
                    tabs = new JSONArray(CmsGalleryConfigValues.TABS_DEFAULTGALLERY.getName());
                } catch (JSONException e) {
                    // should not happen
                    resTypes = new JSONArray();
                    tabs = new JSONArray();
                    if (LOG.isErrorEnabled()) {
                        //TODO: improve error handling
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                setResourceTypes(resTypes);
                setTabs(tabs);
                setImagegallery(false);
                // set start tab
                setStartupTabId(CmsGallerySearchServer.TabId.cms_tab_galleries.toString());
                // set the prefix for the download button
                setButtonPrefix(CmsGalleryConfigValues.DEFAULT.getName());
        }
    }

    /**
     * Sets the prefix for button of the gallery.<p>
     *
     * @param buttonPrefix the prefix for the button
     */
    protected void setButtonPrefix(String buttonPrefix) {

        m_buttonPrefix = buttonPrefix;
    }

    /**
     * Sets the optional class name for generating dynamic configurations, must implement {@link I_CmsGalleryWidgetDynamicConfiguration}.<p>
     * 
     * @param className the optional class name for generating dynamic configurations
     */
    protected void setClassName(String className) {

        m_className = className;
    }

    /**
     * Sets the common parameter for the default gallery configuration. <p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @param jsonObj configuration as json obj
     */
    protected void setDefaultgalleryConfigPart(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param,
        JSONObject jsonObj) {

        if (jsonObj == null) {
            // no configuration
            return;
        }

        // determine the class name that fills in values dynamically
        setClassName(jsonObj.optString(CmsGalleryConfigParam.CONFIG_KEY_CLASS.getName(), null));
        I_CmsGalleryWidgetDynamicConfiguration dynConf = null;
        if (getClassName() != null) {
            try {
                dynConf = (I_CmsGalleryWidgetDynamicConfiguration)Class.forName(getClassName()).newInstance();
            } catch (Exception e) {
                // class not found
            }
        }
        // determine the initial item list settings
        setType(jsonObj.optString(CmsGalleryConfigParam.CONFIG_KEY_TYPE.getName()));
        if ((CmsGalleryConfigParam.CONFIG_VALUE_DYNAMIC.getName().equals(getType()) || CmsStringUtil.isEmpty(getType()))
            && (dynConf != null)) {
            setType(dynConf.getType(cms, widgetDialog, param));
        }
        setStartup(jsonObj.optString(CmsGalleryConfigParam.CONFIG_KEY_STARTUP.getName(), null));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getStartup())) {
            if (getStartup().startsWith("[") && getStartup().endsWith("]")) {
                setStartUpFolders(jsonObj.optJSONArray(CmsGalleryConfigParam.CONFIG_KEY_STARTUP.getName()));
            }
        }
        if ((CmsGalleryConfigParam.CONFIG_VALUE_DYNAMIC.getName().equals(getStartup()) || CmsStringUtil.isEmpty(getStartup()))
            && (dynConf != null)) {
            setStartup(dynConf.getStartup(cms, widgetDialog, param));
        }
    }

    /**
     * Sets the list of image format values matching the options for the format select box.<p>
     * 
     * @param formatValues the list of image format values matching the options for the format select box
     */
    protected void setFormatValues(List formatValues) {

        m_formatValues = formatValues;
    }

    /**
     * Sets the flag for imagegallery. <p>
     *
     * @param isImagegallery flag to set
     */
    protected void setImagegallery(boolean isImagegallery) {

        m_isImagegallery = isImagegallery;
    }

    /**
     * Sets the specific parameter for the image gallery configuration. <p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @param jsonObj configuration as json obj
     */
    protected void prepareAndSetImageGalleryConfigParams(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param,
        JSONObject jsonObj) {

        if (jsonObj == null) {
            // no configuration
            return;
        }

        JSONObject configJsonObj = jsonObj.optJSONObject(CmsGalleryConfigKeys.imagegallery.toString());

        setImageGalleryConfigParams(cms, widgetDialog, param, configJsonObj);

    }

    /**
     * Sets the specific parameter for the image gallery configuration. <p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @param jsonObj configuration as json obj
     */
    protected void setImageGalleryConfigParams(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param,
        JSONObject jsonObj) {

        if (jsonObj == null) {
            // no configuration
            return;
        }

        // determine the class name that fills in values dynamically
        setClassName(jsonObj.optString(CmsGalleryConfigParam.CONFIG_KEY_CLASS.getName(), null));
        I_CmsImageWidgetDynamicConfiguration dynConf = null;
        if (getClassName() != null) {
            try {
                dynConf = (I_CmsImageWidgetDynamicConfiguration)Class.forName(getClassName()).newInstance();
            } catch (Exception e) {
                // class not found
            }
        }
        // determine if the description field should be shown
        // setShowDescription(jsonObj.optBoolean(CmsGalleryConfigParam.CONFIG_KEY_USEDESCRIPTION.getName()));
        // determine if the format select box should be shown
        setShowFormat(jsonObj.optBoolean(CmsGalleryConfigParam.CONFIG_KEY_USEFORMAT.getName()));
        if (isShowFormat()) {
            // only parse options if the format select box should be shown
            String optionsStr = (String)jsonObj.opt(CmsGalleryConfigParam.CONFIG_KEY_FORMATNAMES.getName());
            setSelectFormatString(optionsStr);
            setSelectFormat(CmsSelectWidgetOption.parseOptions(optionsStr));
            // get the corresponding format values as well
            JSONArray formatValues = jsonObj.optJSONArray(CmsGalleryConfigParam.CONFIG_KEY_FORMATVALUES.getName());
            if (formatValues != null) {
                List formatValueList = new ArrayList(formatValues.length());
                for (int i = 0; i < formatValues.length(); i++) {
                    formatValueList.add(formatValues.optString(i));
                }
                setFormatValues(formatValueList);
            }
            if (dynConf != null) {
                setFormatValues(dynConf.getFormatValues(cms, widgetDialog, param, getSelectFormat(), getFormatValues()));
            }
        }
        // determine the initial image list settings
        setType(jsonObj.optString(CmsGalleryConfigParam.CONFIG_KEY_TYPE.getName()));
        if ((CmsGalleryConfigParam.CONFIG_VALUE_DYNAMIC.getName().equals(getType()) || CmsStringUtil.isEmpty(getType()))
            && (dynConf != null)) {
            setType(dynConf.getType(cms, widgetDialog, param));
        }
        setStartup(jsonObj.optString(CmsGalleryConfigParam.CONFIG_KEY_STARTUP.getName(), null));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getStartup())) {
            if (getStartup().startsWith("[") && getStartup().endsWith("]")) {
                setStartUpFolders(jsonObj.optJSONArray(CmsGalleryConfigParam.CONFIG_KEY_STARTUP.getName()));
                LOG.debug(jsonObj.optJSONArray(CmsGalleryConfigParam.CONFIG_KEY_STARTUP.getName()).toString());
            }
        }
        if ((CmsGalleryConfigParam.CONFIG_VALUE_DYNAMIC.getName().equals(getStartup()) || CmsStringUtil.isEmpty(getStartup()))
            && (dynConf != null)) {
            setStartup(dynConf.getStartup(cms, widgetDialog, param));
        }
        // determine the scale parameters
        setScaleParams(jsonObj.optString(CmsGalleryConfigParam.CONFIG_KEY_SCALEPARAMS.getName()));
    }

    /**
     * Sets the resourceTypes.<p>
     *
     * @param array the resourceTypes to set
     */
    protected void setResourceTypes(JSONArray array) {

        m_resourceTypes = array;
    }

    /**
     * Sets the scale parameters to apply to a scaled image (e.g. quality, type).<p>
     * 
     * @param scaleParams the scale parameters to apply to a scaled image
     */
    protected void setScaleParams(String scaleParams) {

        m_scaleParams = scaleParams;
    }

    /**
     * Sets the list of select options for the format select box, must contain {@link CmsSelectWidgetOption} objects.<p>
     * 
     * @param selectFormat the list of select options for the format select box
     */
    protected void setSelectFormat(List selectFormat) {

        m_selectFormat = selectFormat;
    }

    /**
     * Sets the select options for the format select box as String.<p>
     * 
     * @param formatString the select options for the format select box as String
     */
    protected void setSelectFormatString(String formatString) {

        m_selectFormatString = formatString;
    }

    /**
     * Sets if the format select box should be shown.<p>
     * 
     * @param showFormat true if the format select box should be shown, otherwise false
     */
    protected void setShowFormat(boolean showFormat) {

        m_showFormat = showFormat;
    }

    /**
     * Sets the required information for the initial item list to load.<p>
     * 
     * If a gallery should be shown, the path to the gallery must be specified,
     * for a category the category path.<p>
     * 
     * @param startup the required information for the initial item list to load
     */
    protected void setStartup(String startup) {

        m_startup = startup;
    }

    /**
     * Sets the startUpFolders. <p>
     *
     * @param startUpFolders the startUpFolders to set
     */
    protected void setStartUpFolders(JSONArray startUpFolders) {

        m_startUpFolders = startUpFolders;
    }

    /**
     * Sets the tabs to be display in the gallery.<p>
     *
     * @param tabs the JSON array with the tabs to set
     */
    protected void setTabs(JSONArray tabs) {

        m_tabs = tabs;
    }

}

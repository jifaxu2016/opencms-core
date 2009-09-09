/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/A_CmsResourceTypeFolderBase.java,v $
 * Date   : $Date: 2009/09/09 15:54:52 $
 * Version: $Revision: 1.24.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsDataNotImplementedException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Resource type descriptor for the type "folder".<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.24.2.1 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsResourceTypeFolderBase extends A_CmsResourceType {

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public A_CmsResourceTypeFolderBase() {

        super();
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    @Override
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource filename, int newType)
    throws CmsException, CmsDataNotImplementedException {

        if (!OpenCms.getResourceManager().getResourceType(newType).isFolder()) {
            // it is not possible to change the type of a folder to a file type
            throw new CmsDataNotImplementedException(Messages.get().container(
                Messages.ERR_CHTYPE_FOLDER_1,
                cms.getSitePath(filename)));
        }
        super.chtype(cms, securityManager, filename, newType);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String, CmsResource.CmsResourceCopyMode)
     */
    @Override
    public void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        CmsResource.CmsResourceCopyMode siblingMode) throws CmsIllegalArgumentException, CmsException {

        // first validate the destination name
        destination = validateFoldername(destination);

        // collect all resources in the folder (but exclude deleted ones)
        List<CmsResource> resources = securityManager.readChildResources(
            cms.getRequestContext(),
            source,
            CmsResourceFilter.IGNORE_EXPIRATION,
            true,
            true);

        // handle the folder itself
        super.copyResource(cms, securityManager, source, destination, siblingMode);

        // now walk through all sub-resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource childResource = resources.get(i);
            String childDestination = destination.concat(childResource.getName());
            // handle child resources
            getResourceType(childResource).copyResource(
                cms,
                securityManager,
                childResource,
                childDestination,
                siblingMode);
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, CmsSecurityManager, java.lang.String, byte[], List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List<CmsProperty> properties) throws CmsException {

        resourcename = validateFoldername(resourcename);
        return super.createResource(cms, securityManager, resourcename, content, properties);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#deleteResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    @Override
    public void deleteResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResourceDeleteMode siblingMode) throws CmsException {

        super.deleteResource(cms, securityManager, resource, siblingMode);

        // update the project resources: 
        List<CmsProject> projects = OpenCms.getOrgUnitManager().getAllManageableProjects(cms, "", true);
        CmsProject project;
        List<String> projectResources;
        Iterator<String> itProjectResources;

        String projectResourceRootPath;
        String deletedResourceRootPath = resource.getRootPath();
        Iterator<CmsProject> itProjects = projects.iterator();
        while (itProjects.hasNext()) {
            project = itProjects.next();
            projectResources = cms.readProjectResources(project);
            itProjectResources = projectResources.iterator();
            while (itProjectResources.hasNext()) {
                projectResourceRootPath = itProjectResources.next();
                if (projectResourceRootPath.startsWith(deletedResourceRootPath)) {

                    // we have to change the project resource: 
                    CmsObject projectCms = OpenCms.initCmsObject(cms);
                    CmsRequestContext context = projectCms.getRequestContext();
                    context.setCurrentProject(project);
                    securityManager.removeResourceFromProject(context, resource);
                }
            }
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        // folders have no loader
        return -1;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#isFolder()
     */
    @Override
    public boolean isFolder() {

        return true;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String)
     */
    @Override
    public void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException, CmsIllegalArgumentException {

        String dest = cms.getRequestContext().addSiteRoot(destination);
        if (!CmsResource.isFolder(dest)) {
            // ensure folder name end's with a / (required for the following comparison)
            dest = dest.concat("/");
        }
        if (resource.getRootPath().equals(dest)) {
            // move to target with same name is not allowed
            throw new CmsVfsException(org.opencms.file.Messages.get().container(
                org.opencms.file.Messages.ERR_MOVE_SAME_NAME_1,
                destination));
        }
        if (dest.startsWith(resource.getRootPath())) {
            // move of folder inside itself is not allowed
            throw new CmsVfsException(org.opencms.file.Messages.get().container(
                org.opencms.file.Messages.ERR_MOVE_SAME_FOLDER_2,
                cms.getSitePath(resource),
                destination));
        }

        // first validate the destination name
        dest = validateFoldername(dest);

        // this has to be read before the move for changing projectresources: 
        // update the project resources: 
        List<CmsProject> projects = OpenCms.getOrgUnitManager().getAllManageableProjects(cms, "", true);
        CmsProject project;
        List<String> projectResources;
        Iterator<String> itProjectResources;
        String projectResourceRootPathTarget;
        String moveResourceRootPath = resource.getRootPath();
        Iterator<CmsProject> itProjects = projects.iterator();
        // we have to change the project resource: 
        CmsObject projectCms = OpenCms.initCmsObject(cms);
        CmsRequestContext context = projectCms.getRequestContext();
        context.setSiteRoot("/");
        /*
        * 1. Moved resource may be part of several project resources. 
        * 2. Moved resource may be part of resources of several projects. 
        * 3. If the single move operation fails, all project resources should remain untouched. 
        * 4. It is not possible to remove a project resource once the move of the resource has been done, 
        *    because the API requires a CmsResource: 
        * 
        * --> Store all project resource movements, finally do all project resource removals, do the resource move 
        *     and then the project resource additions. 
        */
        List<CmsProjectResourceMoveData> projectResourceMoveList = new ArrayList<CmsProjectResourceMoveData>();
        while (itProjects.hasNext()) {
            project = itProjects.next();
            context.setCurrentProject(project);
            projectResources = cms.readProjectResources(project);
            itProjectResources = projectResources.iterator();
            while (itProjectResources.hasNext()) {
                projectResourceRootPathTarget = itProjectResources.next();
                if (projectResourceRootPathTarget.startsWith(moveResourceRootPath)) {
                    CmsResource deleteProjectResource = null;
                    CmsResource addProjectResource = null;

                    // compute the project resource (it could be a subpath of the moved folder): 
                    String projectResourceRootPathSource = resource.getRootPath()
                        + projectResourceRootPathTarget.substring(moveResourceRootPath.length());

                    // compute the new full project resource path: 
                    projectResourceRootPathTarget = dest
                        + projectResourceRootPathTarget.substring(moveResourceRootPath.length());

                    if (projectResourceRootPathTarget.equals(moveResourceRootPath)) {
                        deleteProjectResource = resource;
                    } else {
                        deleteProjectResource = projectCms.readResource(
                            projectResourceRootPathSource,
                            CmsResourceFilter.ALL);
                    }

                    // impossible to set root path on resource: copy
                    addProjectResource = new CmsResource(
                        resource.getStructureId(),
                        resource.getResourceId(),
                        projectResourceRootPathTarget,
                        resource.getTypeId(),
                        resource.isFolder(),
                        resource.getFlags(),
                        resource.getProjectLastModified(),
                        resource.getState(),
                        resource.getDateCreated(),
                        resource.getUserCreated(),
                        resource.getDateLastModified(),
                        resource.getUserLastModified(),
                        resource.getDateReleased(),
                        resource.getDateExpired(),
                        resource.getSiblingCount(),
                        resource.getLength(),
                        resource.getDateContent(),
                        resource.getVersion()

                    );
                    projectResourceMoveList.add(new CmsProjectResourceMoveData(
                        project,
                        deleteProjectResource,
                        addProjectResource));
                }
            }

        }

        // Action:
        // 1. remove project resources: 
        CmsProjectResourceMoveData projectResourceMoveData;
        Iterator<CmsProjectResourceMoveData> projectMoveResourceIt = projectResourceMoveList.iterator();
        while (projectMoveResourceIt.hasNext()) {
            projectResourceMoveData = projectMoveResourceIt.next();
            context.setCurrentProject(projectResourceMoveData.getProject());
            securityManager.removeResourceFromProject(context, projectResourceMoveData.getSourceResource());
        }
        // 2. move the resource:
        try {
            securityManager.moveResource(cms.getRequestContext(), resource, dest);

            // 3. add the moved resource
            projectMoveResourceIt = projectResourceMoveList.iterator();
            while (projectMoveResourceIt.hasNext()) {
                projectResourceMoveData = projectMoveResourceIt.next();
                context.setCurrentProject(projectResourceMoveData.getProject());
                securityManager.copyResourceToProject(context, projectResourceMoveData.getTargetResource());
            }
        } catch (CmsException consistancyCheck) {
            // moving failed, rollback of removed project resources: 
            projectMoveResourceIt = projectResourceMoveList.iterator();
            while (projectMoveResourceIt.hasNext()) {
                projectResourceMoveData = projectMoveResourceIt.next();
                context.setCurrentProject(projectResourceMoveData.getProject());
                securityManager.copyResourceToProject(context, projectResourceMoveData.getSourceResource());
            }
            throw consistancyCheck;
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     */
    @Override
    public void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsDataNotImplementedException {

        if (type != getTypeId()) {
            // it is not possible to replace a folder with a different type
            throw new CmsDataNotImplementedException(Messages.get().container(
                Messages.ERR_REPLACE_RESOURCE_FOLDER_1,
                cms.getSitePath(resource)));
        }
        // properties of a folder can be replaced, content is ignored
        super.replaceResource(cms, securityManager, resource, getTypeId(), null, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setDateExpired(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    @Override
    public void setDateExpired(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException {

        // handle the folder itself
        super.setDateExpired(cms, securityManager, resource, dateLastModified, recursive);

        if (recursive) {
            // collect all resources in the folder (but exclude deleted ones)
            List<CmsResource> resources = securityManager.readChildResources(
                cms.getRequestContext(),
                resource,
                CmsResourceFilter.IGNORE_EXPIRATION,
                true,
                true);

            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                // handle child resources
                getResourceType(childResource).setDateExpired(
                    cms,
                    securityManager,
                    childResource,
                    dateLastModified,
                    recursive);
            }
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setDateLastModified(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    @Override
    public void setDateLastModified(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException {

        // handle the folder itself
        super.setDateLastModified(cms, securityManager, resource, dateLastModified, recursive);

        if (recursive) {
            // collect all resources in the folder (but exclude deleted ones)
            List<CmsResource> resources = securityManager.readChildResources(
                cms.getRequestContext(),
                resource,
                CmsResourceFilter.IGNORE_EXPIRATION,
                true,
                true);

            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                // handle child resources
                getResourceType(childResource).setDateLastModified(
                    cms,
                    securityManager,
                    childResource,
                    dateLastModified,
                    recursive);
            }
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setDateReleased(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    @Override
    public void setDateReleased(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException {

        // handle the folder itself
        super.setDateReleased(cms, securityManager, resource, dateLastModified, recursive);

        if (recursive) {
            // collect all resources in the folder (but exclude deleted ones)
            List<CmsResource> resources = securityManager.readChildResources(
                cms.getRequestContext(),
                resource,
                CmsResourceFilter.IGNORE_EXPIRATION,
                true,
                true);

            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                // handle child resources
                getResourceType(childResource).setDateReleased(
                    cms,
                    securityManager,
                    childResource,
                    dateLastModified,
                    recursive);
            }
        }
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#undelete(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, boolean)
     */
    @Override
    public void undelete(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, boolean recursive)
    throws CmsException {

        // handle the folder itself
        super.undelete(cms, securityManager, resource, recursive);

        if (recursive) {
            // collect all resources in the folder (but exclude deleted ones)
            List<CmsResource> resources = securityManager.readChildResources(
                cms.getRequestContext(),
                resource,
                CmsResourceFilter.ALL,
                true,
                true);

            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                // handle child resources
                getResourceType(childResource).undelete(cms, securityManager, childResource, recursive);
            }
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceUndoMode)
     */
    @Override
    public void undoChanges(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResource.CmsResourceUndoMode mode) throws CmsException {

        boolean recursive = mode.isRecursive();
        if (mode == CmsResource.UNDO_MOVE_CONTENT) {
            // undo move only?
            String originalPath = securityManager.resourceOriginalPath(cms.getRequestContext(), resource);
            if (originalPath.equals(resource.getRootPath())) {
                // resource not moved
                recursive = false;
            }
        }

        List<CmsResource> resources = null;
        if (recursive) { // recursive?
            // collect all resources in the folder (including deleted ones)
            resources = securityManager.readChildResources(
                cms.getRequestContext(),
                resource,
                CmsResourceFilter.ALL,
                true,
                true);
        }

        // handle the folder itself, undo move op
        super.undoChanges(cms, securityManager, resource, mode);

        // the folder may have been moved back to its original position        
        CmsResource undoneResource2 = securityManager.readResource(
            cms.getRequestContext(),
            resource.getStructureId(),
            CmsResourceFilter.ALL);
        boolean isMoved = !undoneResource2.getRootPath().equals(resource.getRootPath());

        if (recursive && (resources != null)) { // recursive?
            // now walk through all sub-resources in the folder, and undo first
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                I_CmsResourceType type = getResourceType(childResource);
                if (isMoved) {
                    securityManager.lockResource(cms.getRequestContext(), childResource, CmsLockType.EXCLUSIVE);
                }
                if (childResource.isFolder()) {
                    // recurse into this method for subfolders
                    type.undoChanges(cms, securityManager, childResource, mode);
                } else if (!childResource.getState().isNew()) {
                    // undo changes for changed files
                    securityManager.undoChanges(cms.getRequestContext(), childResource, mode);
                } else {
                    // undo move for new files? move with the folder
                    if (mode.isUndoMove()) {
                        String newPath = cms.getRequestContext().removeSiteRoot(
                            securityManager.readResource(
                                cms.getRequestContext(),
                                resource.getStructureId(),
                                CmsResourceFilter.ALL).getRootPath()
                                + childResource.getName());
                        type.moveResource(cms, securityManager, childResource, newPath);
                    }
                }
            }

            // now iterate again all sub-resources in the folder, and actualize the relations
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                updateRelationForUndo(cms, securityManager, childResource);
            }
        }
    }

    /**
     * Checks if there are at least one character in the folder name,
     * also ensures that it starts and ends with a '/'.<p>
     *
     * @param resourcename folder name to check (complete path)
     * 
     * @return the validated folder name
     * 
     * @throws CmsIllegalArgumentException if the folder name is empty or <code>null</code>
     */
    private String validateFoldername(String resourcename) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmpty(resourcename)) {
            throw new CmsIllegalArgumentException(org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_BAD_RESOURCENAME_1,
                resourcename));
        }
        if (!CmsResource.isFolder(resourcename)) {
            resourcename = resourcename.concat("/");
        }
        if (resourcename.charAt(0) != '/') {
            resourcename = "/".concat(resourcename);
        }
        return resourcename;
    }
}
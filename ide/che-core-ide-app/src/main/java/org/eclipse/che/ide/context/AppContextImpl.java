/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.context;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.app.StartUpAction;
import org.eclipse.che.ide.api.data.HasDataObject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.event.WindowActionHandler;
import org.eclipse.che.ide.api.machine.ActiveRuntime;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.ResourcePathComparator;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.ResourceManagerInitializer;
import org.eclipse.che.ide.resources.impl.ResourceDeltaImpl;
import org.eclipse.che.ide.resources.impl.ResourceManager;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.util.Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.sort;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.SYNCHRONIZED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

/**
 * Implementation of {@link AppContext}.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class AppContextImpl implements AppContext,
                                       SelectionChangedHandler,
                                       ResourceChangedHandler,
                                       WindowActionHandler,
                                       WorkspaceStartedEvent.Handler,
                                       WorkspaceStoppedEvent.Handler,
                                       ResourceManagerInitializer {

    private static final Project[] NO_PROJECTS = {};

    private final QueryParameters queryParameters;
    private final List<String>    projectsInImport;

    private Workspace           usersWorkspace;
    private CurrentUser         currentUser;
    private FactoryDto          factory;
    private Path                projectsRoot;
    private ActiveRuntime       runtime;
    /**
     * List of actions with parameters which comes from startup URL.
     * Can be processed after IDE initialization as usual after starting ws-agent.
     */
    private List<StartUpAction> startAppActions;

    private Resource   currentResource;
    private Resource[] currentResources;

    @Inject
    public AppContextImpl(EventBus eventBus,
                          QueryParameters queryParameters,
                          ResourceManager.ResourceManagerFactory resourceManagerFactory,
                          Provider<EditorAgent> editorAgentProvider,
                          Provider<AppStateManager> appStateManager) {
        this.eventBus = eventBus;
        this.queryParameters = queryParameters;
        this.resourceManagerFactory = resourceManagerFactory;
        this.editorAgentProvider = editorAgentProvider;
        this.appStateManager = appStateManager;

        projectsInImport = new ArrayList<>();

        eventBus.addHandler(SelectionChangedEvent.TYPE, this);
        eventBus.addHandler(ResourceChangedEvent.getType(), this);
        eventBus.addHandler(WindowActionEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    @Override
    public Workspace getWorkspace() {
        return usersWorkspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        if (workspace != null) {
            usersWorkspace = workspace;
            runtime = new ActiveRuntime(workspace.getRuntime());
        } else {
            usersWorkspace = null;
            runtime = null;
        }
    }

    @Override
    public String getWorkspaceId() {
        if (usersWorkspace == null) {
            throw new IllegalArgumentException(getClass() + " Workspace can not be null.");
        }

        return usersWorkspace.getId();
    }

    @Override
    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public List<String> getImportingProjects() {
        return projectsInImport;
    }

    @Override
    public void addProjectToImporting(String pathToProject) {
        projectsInImport.add(pathToProject);
    }

    @Override
    public void removeProjectFromImporting(String pathToProject) {
        projectsInImport.remove(pathToProject);
    }

    @Override
    public List<StartUpAction> getStartAppActions() {
        return startAppActions;
    }

    @Override
    public void setStartUpActions(List<StartUpAction> startUpActions) {
        this.startAppActions = startUpActions;
    }

    @Override
    public FactoryDto getFactory() {
        return factory;
    }

    @Override
    public void setFactory(FactoryDto factory) {
        this.factory = factory;
    }

    @Override
    public DevMachine getDevMachine() {
        return runtime.getDevMachine();
    }


    @Override
    public void initResourceManager(final Callback<ResourceManager, Exception> callback) {
        if (runtime.getDevMachine() == null) {
            //should never happened, but anyway
            callback.onFailure(new NullPointerException("Dev machine is not initialized"));
        }

        if (projects != null) {
            for (Project project : projects) {
                eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(project, REMOVED)));
            }
            projects = null;
        }

        resourceManager = resourceManagerFactory.newResourceManager(runtime.getDevMachine());
        resourceManager.getWorkspaceProjects().then(new Operation<Project[]>() {
            @Override
            public void apply(Project[] projects) throws OperationException {
                AppContextImpl.this.projects = projects;
                java.util.Arrays.sort(AppContextImpl.this.projects, ResourcePathComparator.getInstance());
                callback.onSuccess(resourceManager);
                eventBus.fireEvent(new WorkspaceReadyEvent(projects));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                callback.onFailure((Exception)error.getCause());
            }
        });
    }

    @Override
    public String getWorkspaceName() {
        return usersWorkspace.getConfig().getName();
    }

    /** {@inheritDoc} */
    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();
        final Resource resource = delta.getResource();

        /* Note: There is important to keep projects array in sorted state, because it is mutable and removing projects from it
           need array to be sorted. Search specific projects realized with binary search. */

        if (!(resource.getResourceType() == PROJECT && resource.getLocation().segmentCount() == 1)) {
            return;
        }

        if (projects == null) {
            return; //Normal situation, workspace config updated and project has not been loaded fully. Just skip this situation.
        }

        if (delta.getKind() == ADDED) {
            Project[] newProjects = copyOf(projects, projects.length + 1);
            newProjects[projects.length] = (Project)resource;
            projects = newProjects;
            sort(projects, ResourcePathComparator.getInstance());
        } else if (delta.getKind() == REMOVED) {
            int size = projects.length;
            int index = java.util.Arrays.binarySearch(projects, resource, ResourcePathComparator.getInstance());
            int numMoved = projects.length - index - 1;
            if (numMoved > 0) {
                System.arraycopy(projects, index + 1, projects, index, numMoved);
            }
            projects = copyOf(projects, --size);

            if (currentResource != null && currentResource.equals(delta.getResource())) {
                currentResource = null;
            }

            if (currentResources != null) {
                for (Resource currentResource : currentResources) {
                    if (currentResource.equals(delta.getResource())) {
                        currentResources = Arrays.remove(currentResources, currentResource);
                    }
                }
            }
        } else if (delta.getKind() == UPDATED) {
            int index = -1;

            // Project may be moved to another location, so we need to remove previous one and store new project in cache.

            if (delta.getFlags() == MOVED_FROM) {
                for (int i = 0; i < projects.length; i++) {
                    if (projects[i].getLocation().equals(delta.getFromPath())) {
                        index = i;
                        break;
                    }
                }
            } else {
                index = binarySearch(projects, resource);
            }

            if (index != -1) {
                projects[index] = (Project)resource;
            }

            sort(projects, ResourcePathComparator.getInstance());
        } else if (delta.getKind() == SYNCHRONIZED && resource.isProject() && resource.getLocation().segmentCount() == 1) {
            for (int i = 0; i < projects.length; i++) {
                if (projects[i].getLocation().equals(resource.getLocation())) {
                    projects[i] = (Project)resource;
                }
            }
        }
    }

    @Override
    public Path getProjectsRoot() {
        return projectsRoot;
    }

    public void setProjectsRoot(Path projectsRoot) {
        this.projectsRoot = projectsRoot;
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent event) {
        final Selection<?> selection = event.getSelection();
        if (selection instanceof Selection.NoSelectionProvided) {
            return;
        }

        currentResource = null;
        currentResources = null;

        if (selection == null || selection.getHeadElement() == null) {
            return;
        }

        final Object headObject = selection.getHeadElement();
        final List<?> allObjects = selection.getAllElements();

        if (headObject instanceof HasDataObject) {
            Object data = ((HasDataObject)headObject).getData();

            if (data instanceof Resource) {
                currentResource = (Resource)data;
            }
        } else if (headObject instanceof Resource) {
            currentResource = (Resource)headObject;
        }

        Set<Resource> resources = Sets.newHashSet();

        for (Object object : allObjects) {
            if (object instanceof HasDataObject) {
                Object data = ((HasDataObject)object).getData();

                if (data instanceof Resource) {
                    resources.add((Resource)data);
                }
            } else if (object instanceof Resource) {
                resources.add((Resource)object);
            }
        }

        currentResources = resources.toArray(new Resource[resources.size()]);
    }

    private final EventBus                               eventBus;
    private final ResourceManager.ResourceManagerFactory resourceManagerFactory;
    private final Provider<EditorAgent>                  editorAgentProvider;
    private final Provider<AppStateManager>              appStateManager;

    private ResourceManager resourceManager;
    private Project[]       projects;

    @Override
    public Project[] getProjects() {
        return projects == null ? new Project[0] : projects;
    }

    @Override
    public Container getWorkspaceRoot() {
        checkState(resourceManager != null, "Workspace configuration has not been received yet");

        return resourceManager.getWorkspaceRoot();
    }

    @Override
    public Resource getResource() {
        return currentResource;
    }

    @Override
    public Resource[] getResources() {
        return currentResources;
    }

    @Override
    public Project getRootProject() {
        if (projects == null) {
            return null;
        }

        if (currentResource == null || currentResources == null) {

            EditorAgent editorAgent = editorAgentProvider.get();
            if (editorAgent == null) {
                return null;
            }

            final EditorPartPresenter editor = editorAgent.getActiveEditor();
            if (editor == null) {
                return null;
            }

            final VirtualFile file = editor.getEditorInput().getFile();

            if (file instanceof SyntheticNode) {
                final Path projectPath = ((SyntheticNode)file).getProject();
                for (Project project : projects) {
                    if (project.getLocation().equals(projectPath)) {
                        return project;
                    }
                }
            }
        }

        if (currentResource == null) {
            return null;
        }

        Project root = null;

        for (Project project : projects) {
            if (project.getLocation().isPrefixOf(currentResource.getLocation())) {
                root = project;
            }
        }

        if (root == null) {
            return null;
        }

        for (int i = 1; i < currentResources.length; i++) {
            if (!root.getLocation().isPrefixOf(currentResources[i].getLocation())) {
                return null;
            }
        }

        return root;
    }

    @Override
    public void onWindowClosing(WindowActionEvent event) {
        appStateManager.get().persistWorkspaceState(getWorkspaceId());
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        setWorkspace(event.getWorkspace());
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        appStateManager.get().persistWorkspaceState(getWorkspaceId()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                for (Project project : projects) {
                    eventBus.fireEvent(new ResourceChangedEvent(new ResourceDeltaImpl(project, REMOVED)));
                }

                projects = NO_PROJECTS; //avoid NPE
                resourceManager = null;
            }
        });

        //goto close all editors
        final EditorAgent editorAgent = editorAgentProvider.get();
        final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        for (EditorPartPresenter editor : openedEditors) {
            editorAgent.closeEditor(editor);
        }
        runtime = null;
    }

    @Override
    public void onWindowClosed(WindowActionEvent event) {
    }

    @Override
    public String getMasterEndpoint() {
        String fromUrl = queryParameters.getByName("master");
        if(fromUrl == null || fromUrl.isEmpty())
            return masterFromIDEConfig();
        else
            return fromUrl;
    }

    @Override
    public String getDevAgentEndpoint() {
        String fromUrl = queryParameters.getByName("agent");
        if(fromUrl == null || fromUrl.isEmpty())
            return runtime.getDevMachine().getWsAgentBaseUrl();
        else
            return fromUrl;
    }

    @Override
    public ActiveRuntime getActiveRuntime() {
        return runtime;
    }


    private static native String masterFromIDEConfig() /*-{
        if ($wnd.IDE && $wnd.IDE.config) {
            return $wnd.IDE.config.restContext;
        } else {
            return null;
        }
    }-*/;
}

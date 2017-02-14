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
package org.eclipse.che.ide.ext.git.client.history;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changedList.ChangedListPresenter;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.MODIFIED;
import static org.eclipse.che.ide.resource.Path.EMPTY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HistoryPresenterTest extends BaseTest {

    @Mock
    private HistoryView          view;
    @Mock
    private ComparePresenter     comparePresenter;
    @Mock
    private ChangedListPresenter changedListPresenter;
    @InjectMocks
    private HistoryPresenter     presenter;

    @Override
    public void disarm() {
        super.disarm();

        Resource resource = mock(Resource.class);
        when(resource.getLocation()).thenReturn(EMPTY);
        when(appContext.getResource()).thenReturn(resource);
        when(appContext.getRootProject()).thenReturn(project);

        when(service.log(any(DevMachine.class),
                         any(Path.class),
                         any(Path[].class),
                         anyInt(),
                         anyInt(),
                         anyBoolean()))
                .thenReturn(logPromise);
        when(service.diff(any(DevMachine.class),
                          any(Path.class),
                          anyList(),
                          any(DiffType.class),
                          anyBoolean(),
                          anyInt(),
                          anyString(),
                          anyString()))
                .thenReturn(stringPromise);
        when(service.showFileContent(any(DevMachine.class),
                                     any(Path.class),
                                     any(Path.class),
                                     anyString()))
                .thenReturn(showPromise);
        when(stringPromise.then(any(Operation.class))).thenReturn(stringPromise);
        when(stringPromise.catchError(any(Operation.class))).thenReturn(stringPromise);
        when(logPromise.then(any(Operation.class))).thenReturn(logPromise);
        when(logPromise.catchError(any(Operation.class))).thenReturn(logPromise);
        when(constant.historyTitle()).thenReturn("title");
        when(constant.historyNothingToDisplay()).thenReturn("error message");
        when(constant.compareReadOnlyTitle()).thenReturn("(Read only)");
    }

    @Test
    public void shouldGetCommitsAndShowDialog() throws Exception {
        LogResponse response = mock(LogResponse.class);
        List<Revision> revisions = singletonList(mock(Revision.class));
        when(response.getCommits()).thenReturn(revisions);

        presenter.show();
        verify(logPromise).then(logCaptor.capture());
        logCaptor.getValue().apply(response);

        verify(view).setRevisions(revisions);
        verify(view).showDialog();
    }

    @Test
    public void shouldShowDialogOnInitCommitError() throws Exception {
        PromiseError error = mock(PromiseError.class);
        ServerException exception = mock(ServerException.class);
        when(exception.getErrorCode()).thenReturn(ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED);
        when(error.getCause()).thenReturn(exception);
        when(constant.initCommitWasNotPerformed()).thenReturn("error message");
        MessageDialog dialog = mock(MessageDialog.class);
        when(dialogFactory.createMessageDialog(eq("title"), eq("error message"), any(ConfirmCallback.class))).thenReturn(dialog);

        presenter.show();
        verify(logPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(error);

        verify(dialog).show();
    }

    @Test
    public void shouldShowNotificationOnGetLogError() throws Exception {
        when(constant.logFailed()).thenReturn("error");

        presenter.show();
        verify(logPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(mock(PromiseError.class));

        verify(notificationManager).notify(eq("error"), eq(FAIL), eq(EMERGE_MODE));
    }

    @Test
    public void shouldShowCompareWhenOneFileChangedInCurrentRevision() throws Exception {
        Revision parentRevision = mock(Revision.class);
        Revision selectedRevision = mock(Revision.class);
        when(parentRevision.getId()).thenReturn("commitA");
        when(selectedRevision.getId()).thenReturn("commitB");
        LogResponse logResponse = mock(LogResponse.class);
        List<Revision> revisions = new ArrayList<>();
        revisions.add(selectedRevision);
        revisions.add(parentRevision);
        when(logResponse.getCommits()).thenReturn(revisions);

        presenter.show();
        presenter.onRevisionSelected(selectedRevision);
        verify(logPromise).then(logCaptor.capture());
        logCaptor.getValue().apply(logResponse);
        presenter.onCompareClicked();
        verify(stringPromise).then(stringCaptor.capture());
        stringCaptor.getValue().apply("M file");

        verify(comparePresenter).showCompareBetweenRevisions(eq(Path.valueOf("file")), eq(MODIFIED), eq("commitA"), eq("commitB"));
    }

    @Test
    public void shouldShowChangedListWhenSeveralFilesChangedInSelectedRevision() throws Exception {
        Revision revisionA = mock(Revision.class);
        Revision revisionB = mock(Revision.class);
        when(revisionA.getId()).thenReturn("commitA");
        when(revisionB.getId()).thenReturn("commitB");
        LogResponse logResponse = mock(LogResponse.class);
        List<Revision> revisions = new ArrayList<>();
        revisions.add(revisionA);
        revisions.add(revisionB);
        when(logResponse.getCommits()).thenReturn(revisions);

        presenter.show();
        presenter.onRevisionSelected(revisionA);
        verify(logPromise).then(logCaptor.capture());
        logCaptor.getValue().apply(logResponse);
        presenter.onCompareClicked();
        verify(stringPromise).then(stringCaptor.capture());
        stringCaptor.getValue().apply("M file1\nM file2");

        verify(changedListPresenter).show(anyMap(), eq("commitB"), eq("commitA"), any(Project.class));
    }

    @Test
    public void shouldShowNotificationOnGetDiffError() throws Exception {
        Revision revisionA = mock(Revision.class);
        Revision revisionB = mock(Revision.class);
        LogResponse logResponse = mock(LogResponse.class);
        List<Revision> revisions = new ArrayList<>();
        revisions.add(revisionA);
        revisions.add(revisionB);
        when(logResponse.getCommits()).thenReturn(revisions);
        when(constant.diffFailed()).thenReturn("error");

        presenter.show();
        presenter.onRevisionSelected(revisionA);
        verify(logPromise).then(logCaptor.capture());
        logCaptor.getValue().apply(logResponse);
        presenter.onCompareClicked();
        verify(stringPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(null);

        verify(notificationManager).notify(eq("error"), eq(FAIL), eq(EMERGE_MODE));
    }

    @Test
    public void shouldShowDialogIfNothingToCompare() throws Exception {
        MessageDialog dialog = mock(MessageDialog.class);
        when(dialogFactory.createMessageDialog(eq("title"), eq("error message"), any(ConfirmCallback.class))).thenReturn(dialog);

        presenter.show();
        presenter.onRevisionSelected(mock(Revision.class));
        presenter.onCompareClicked();
        verify(stringPromise).then(stringCaptor.capture());
        stringCaptor.getValue().apply("");

        verify(dialog).show();
    }
}

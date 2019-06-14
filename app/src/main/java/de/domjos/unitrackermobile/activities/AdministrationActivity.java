/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.activities;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.AdministrationTask;
import de.domjos.unibuggerlibrary.tasks.FieldTask;
import de.domjos.unibuggerlibrary.tasks.IssueTask;
import de.domjos.unibuggerlibrary.tasks.ProjectTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.settings.Settings;

public final class AdministrationActivity extends AbstractActivity {
    private Button cmdCopy, cmdMove;
    private Spinner spBugTracker1, spBugTracker2, spProject1, spProject2, spData1, spDataItem1;
    private ArrayAdapter<Authentication> bugTrackerAdapter1, bugTrackerAdapter2;
    private ArrayAdapter<Project> projectAdapter1, projectAdapter2;
    private ArrayAdapter<DescriptionObject> dataItemAdapter1;
    private ArrayAdapter<String> dataAdapter1;
    private CheckBox chkWithIssues;
    private IBugService bugService1, bugService2;

    private Context ctx;
    private Settings settings;

    public AdministrationActivity() {
        super(R.layout.administration_activity);
    }

    @Override
    protected void initActions() {
        this.spBugTracker1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    projectAdapter1.clear();
                    Authentication authentication = bugTrackerAdapter1.getItem(position);
                    bugService1 = Helper.getCurrentBugService(authentication, ctx);

                    boolean showData = false;
                    ProjectTask projectTask = new ProjectTask(AdministrationActivity.this, bugService1, false, settings.showNotifications());
                    for (Object object : projectTask.execute(0L).get()) {
                        projectAdapter1.add((Project) object);
                        showData = true;
                    }

                    if (showData) {
                        dataAdapter1.clear();
                        dataAdapter1.addAll(Arrays.asList(getResources().getStringArray(R.array.administration_data)));
                    }
                    checkPermissions();
                } catch (Exception ex) {
                    MessageHelper.printException(ex, AdministrationActivity.this);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spBugTracker2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    projectAdapter2.clear();
                    Authentication authentication = bugTrackerAdapter2.getItem(position);
                    bugService2 = Helper.getCurrentBugService(authentication, ctx);

                    ProjectTask projectTask = new ProjectTask(AdministrationActivity.this, bugService2, false, settings.showNotifications());
                    for (Object object : projectTask.execute(0L).get()) {
                        projectAdapter2.add((Project) object);
                    }
                    checkPermissions();
                } catch (Exception ex) {
                    MessageHelper.printException(ex, AdministrationActivity.this);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spProject1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reloadData1(spData1.getSelectedItemPosition(), position);
                checkPermissions();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spData1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reloadData1(position, spProject1.getSelectedItemPosition());
                checkPermissions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.chkWithIssues.setOnCheckedChangeListener((buttonView, isChecked) -> checkPermissions());
        this.cmdCopy.setOnClickListener((v) -> this.writeData(false));
        this.cmdMove.setOnClickListener((v) -> this.writeData(true));
    }

    private void checkPermissions() {
        if (this.bugService1 != null) {
            if (this.bugService1.getPermissions() != null) {
                IFunctionImplemented func = this.bugService1.getPermissions();

                switch (this.spData1.getSelectedItemPosition()) {
                    case 0:
                        this.cmdMove.setEnabled(func.deleteProjects() && func.listProjects());
                        this.cmdCopy.setEnabled(func.listProjects());
                        if (this.chkWithIssues.isChecked()) {
                            if (this.cmdMove.isEnabled()) {
                                this.cmdMove.setEnabled(func.deleteIssues() && func.listIssues());
                            }
                            if (this.cmdCopy.isEnabled()) {
                                this.cmdCopy.setEnabled(func.listIssues());
                            }
                        }
                        break;
                    case 1:
                        this.cmdMove.setEnabled(func.deleteIssues() && func.listIssues());
                        this.cmdCopy.setEnabled(func.listIssues());
                        break;
                    case 2:
                        this.cmdMove.setEnabled(func.deleteCustomFields() && func.listCustomFields());
                        this.cmdCopy.setEnabled(func.listCustomFields());
                        break;
                }
            }
        }

        if (this.bugService2 != null) {
            if (this.bugService2.getPermissions() != null) {
                IFunctionImplemented func = this.bugService2.getPermissions();

                switch (this.spData1.getSelectedItemPosition()) {
                    case 0:
                        this.cmdMove.setEnabled(this.cmdMove.isEnabled() && func.addProjects());
                        this.cmdCopy.setEnabled(this.cmdCopy.isEnabled() && func.addProjects());
                        if (this.chkWithIssues.isChecked()) {
                            if (this.cmdMove.isEnabled()) {
                                this.cmdMove.setEnabled(func.deleteIssues() && func.addIssues());
                            }
                            if (this.cmdCopy.isEnabled()) {
                                this.cmdCopy.setEnabled(func.addIssues());
                            }
                        }
                        break;
                    case 1:
                        this.cmdMove.setEnabled(this.cmdMove.isEnabled() && func.addIssues());
                        this.cmdCopy.setEnabled(this.cmdCopy.isEnabled() && func.addIssues());
                        break;
                    case 2:
                        this.cmdMove.setEnabled(this.cmdMove.isEnabled() && func.addCustomFields());
                        this.cmdCopy.setEnabled(this.cmdCopy.isEnabled() && func.addCustomFields());
                        break;
                }
            }
        }
    }

    private void reloadData1(int data, int projectPosition) {
        try {
            boolean notify = this.settings.showNotifications();
            Project project1 = this.projectAdapter1.getItem(projectPosition);
            if (project1 != null) {
                this.dataItemAdapter1.clear();
                switch (data) {
                    case 0:
                        ProjectTask projectTask = new ProjectTask(AdministrationActivity.this, this.bugService1, false, notify);
                        this.dataItemAdapter1.addAll(projectTask.execute(0L).get());
                        for (int i = 0; i <= dataItemAdapter1.getCount() - 1; i++) {
                            DescriptionObject descriptionObject = dataItemAdapter1.getItem(i);
                            if (descriptionObject != null) {
                                if (descriptionObject.getId().equals(project1.getId())) {
                                    spDataItem1.setSelection(i);
                                    break;
                                }
                            }
                        }
                        break;
                    case 1:
                        IssueTask issueTask = new IssueTask(AdministrationActivity.this, this.bugService1, project1.getId(), false, false, notify);
                        this.dataItemAdapter1.addAll(issueTask.execute(0L).get());
                        break;
                    case 2:
                        FieldTask fieldTask = new FieldTask(AdministrationActivity.this, this.bugService1, project1.getId(), false, notify);
                        this.dataItemAdapter1.addAll(fieldTask.execute(0L).get());
                        break;
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }

    private void writeData(boolean move) {
        try {
            Activity act = AdministrationActivity.this;
            boolean notify = this.settings.showNotifications();

            Project project2;
            if (this.spProject2.getSelectedItemPosition() != -1) {
                project2 = this.projectAdapter2.getItem(this.spProject2.getSelectedItemPosition());
            } else {
                project2 = new Project();
            }
            Project project1 = this.projectAdapter1.getItem(this.spProject1.getSelectedItemPosition());
            DescriptionObject dataItem1 = this.dataItemAdapter1.getItem(this.spDataItem1.getSelectedItemPosition());
            int dataPosition = this.spData1.getSelectedItemPosition();

            AdministrationTask administrationTask = new AdministrationTask(act, notify, move, chkWithIssues.isChecked(), project1, project2, dataItem1, dataPosition);
            administrationTask.execute(bugService1, bugService2).get();
            this.reloadAuthentications();

            String message = String.format(
                    this.getString(R.string.administration_message),
                    this.getResources().getStringArray(R.array.administration_data)[this.spData1.getSelectedItemPosition()],
                    move ? this.getString(R.string.administration_move) : this.getString(R.string.administration_copy)
            );
            MessageHelper.printMessage(message, this.ctx);
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }


    private void reloadAuthentications() {
        List<Authentication> authentications = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("");

        this.bugTrackerAdapter1.clear();
        this.bugTrackerAdapter1.addAll(authentications);

        this.bugTrackerAdapter2.clear();
        this.bugTrackerAdapter2.addAll(authentications);
    }
    @Override
    protected void initControls() {
        int spinner = android.R.layout.simple_spinner_item;
        this.ctx = this.getApplicationContext();
        this.settings = MainActivity.GLOBALS.getSettings(this.ctx);
        this.cmdCopy = this.findViewById(R.id.cmdCopy);
        this.cmdMove = this.findViewById(R.id.cmdMove);



        this.spBugTracker1 = this.findViewById(R.id.spBugTracker1);
        this.bugTrackerAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spBugTracker1.setAdapter(this.bugTrackerAdapter1);
        this.bugTrackerAdapter1.notifyDataSetChanged();
        this.spBugTracker2 = this.findViewById(R.id.spBugTracker2);
        this.bugTrackerAdapter2 = new ArrayAdapter<>(ctx, spinner);
        this.spBugTracker2.setAdapter(this.bugTrackerAdapter2);
        this.bugTrackerAdapter2.notifyDataSetChanged();
        this.reloadAuthentications();

        this.spProject1 = this.findViewById(R.id.spProject1);
        this.projectAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spProject1.setAdapter(this.projectAdapter1);
        this.projectAdapter1.notifyDataSetChanged();
        this.spProject2 = this.findViewById(R.id.spProject2);
        this.projectAdapter2 = new ArrayAdapter<>(ctx, spinner);
        this.spProject2.setAdapter(this.projectAdapter2);
        this.projectAdapter2.notifyDataSetChanged();

        this.spData1 = this.findViewById(R.id.spData1);
        this.dataAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spData1.setAdapter(this.dataAdapter1);
        this.dataAdapter1.notifyDataSetChanged();

        this.spDataItem1 = this.findViewById(R.id.spDataItem1);
        this.dataItemAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spDataItem1.setAdapter(this.dataItemAdapter1);
        this.dataItemAdapter1.notifyDataSetChanged();

        this.chkWithIssues = this.findViewById(R.id.chkWithIssues);
    }
}

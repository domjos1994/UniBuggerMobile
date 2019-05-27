/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
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
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggerlibrary.services.tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.permissions.GithubPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class Github extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;
    private String title;
    private String project;

    public Github(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;

    }

    @Override
    public boolean testConnection() {
        return true;
    }

    @Override
    public String getTrackerVersion() {
        return "v3";
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/users/" + this.authentication.getUserName() + "/repos");

        if (status == 200 || status == 201) {
            JSONArray versionArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= versionArray.length() - 1; i++) {
                Project<Long> project = new Project<>();
                JSONObject jsonObject = versionArray.getJSONObject(i);
                project.setId(jsonObject.getLong("id"));
                project.setTitle(jsonObject.getString("full_name"));
                project.setAlias(jsonObject.getString("name"));
                project.setPrivateProject(jsonObject.getBoolean("private"));
                project.setDescription(jsonObject.getString("description"));
                project.setWebsite(jsonObject.getString("homepage"));
                project.setEnabled(!jsonObject.getBoolean("disabled"));

                if (jsonObject.has("created_at")) {
                    Date dt = Converter.convertStringToDate(jsonObject.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'");
                    if (dt != null) {
                        project.setCreatedAt(dt.getTime());
                    }
                }
                if (jsonObject.has("updated_at")) {
                    Date dt = Converter.convertStringToDate(jsonObject.getString("updated_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'");
                    if (dt != null) {
                        project.setUpdatedAt(dt.getTime());
                    }
                }
                projects.add(project);
            }
        }

        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        List<Project<Long>> projects = this.getProjects();
        for (Project<Long> project : projects) {
            if (project.getId().equals(id)) {
                return project;
            }
        }
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", project.getAlias());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("homepage", project.getWebsite());
        jsonObject.put("private", project.isPrivateProject());
        jsonObject.put("has_issues", true);
        jsonObject.put("has_projects", false);
        jsonObject.put("has_wiki", false);

        String method;
        String url;
        if (project.getId() == null) {
            url = "/user/repos";
            method = "POST";
        } else {
            url = "/repos/" + project.getTitle();
            method = "POST";
        }

        int status = this.executeRequest(url, jsonObject.toString(), method);
        if (status == 200 || status == 201) {
            JSONObject response = new JSONObject(this.getCurrentMessage());
            return response.getLong("id");
        }

        return 0L;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        Project<Long> project = this.getProject(id);
        if (project != null) {
            this.deleteRequest("/repos/" + project.getTitle());
        }
    }

    @Override
    public List<Version<Long>> getVersions(Long pid, String filter) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        Project<Long> project = this.getProject(pid);
        if (project != null) {
            int status = this.executeRequest("/repos/" + this.authentication.getUserName() + "/" + project.getAlias() + "/releases");

            if (status == 200 || status == 201) {
                JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Version<Long> version = new Version<>();
                    version.setTitle(jsonObject.getString("name"));
                    version.setDescription(jsonObject.getString("body"));
                    version.setReleasedVersionAt(Converter.convertStringToDate(jsonObject.getString("published_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime());
                    version.setId(jsonObject.getLong("id"));
                    version.setReleasedVersion(jsonObject.getBoolean("prerelease"));
                    versions.add(version);
                }
            }
        }

        return versions;
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tag_name", version.getTitle());
        jsonObject.put("name", version.getTitle());
        jsonObject.put("body", version.getDescription());
        jsonObject.put("draft", true);
        jsonObject.put("prerelease", version.isReleasedVersion());

        String method;
        String url;
        Project<Long> project = this.getProject(pid);
        if (project != null) {
            if (version.getId() == null) {
                url = "/repos/" + this.authentication.getUserName() + "/" + project.getAlias() + "/releases";
                method = "POST";
            } else {
                url = "/repos/" + this.authentication.getUserName() + "/" + project.getAlias() + "/releases/" + version.getId();
                method = "PATCH";
            }

            int status = this.executeRequest(url, jsonObject.toString(), method);

            if (status == 200 || status == 201) {
                JSONObject response = new JSONObject(this.getCurrentMessage());
                return response.getLong("id");
            }
        }

        return null;
    }

    @Override
    public void deleteVersion(Long id) throws Exception {
        this.deleteRequest("/repos/" + this.authentication.getUserName() + "/" + this.title + "/releases/" + id);
    }

    @Override
    public List<Issue<Long>> getIssues(Long pid) throws Exception {
        List<Issue<Long>> issues = new LinkedList<>();
        Project<Long> project = this.getProject(pid);

        if (project != null) {
            int status = this.executeRequest("/repos/" + project.getTitle() + "/issues");
            if (status == 200 || status == 201) {
                JSONArray issueArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= issueArray.length() - 1; i++) {
                    Issue<Long> issue = new Issue<>();
                    JSONObject issueObject = issueArray.getJSONObject(i);
                    issue.setId(issueObject.getLong("number"));
                    issue.setTitle(issueObject.getString("title"));
                    issue.setDescription(issueObject.getString("body"));
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    public Issue<Long> getIssue(Long id, String title) throws Exception {
        this.project = title;
        return this.getIssue(id);
    }

    @Override
    public Issue<Long> getIssue(Long id) throws Exception {
        Issue<Long> issue = new Issue<>();
        int status = this.executeRequest("/repos/" + this.project + "/issues/" + id);
        if (status == 200 || status == 201) {
            JSONObject issueObject = new JSONObject(this.getCurrentMessage());
            issue.setId(issueObject.getLong("number"));
            issue.setTitle(issueObject.getString("title"));
            issue.setDescription(issueObject.getString("body"));
            issue.setLastUpdated(Converter.convertStringToDate(issueObject.getString("updated_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
            issue.setSubmitDate(Converter.convertStringToDate(issueObject.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
            issue.setHandler(this.getUser(issueObject.getJSONObject("user")));
        }
        return issue;
    }

    @Override
    public Long insertOrUpdateIssue(Long pid, Issue<Long> issue) throws Exception {
        return null;
    }

    @Override
    public void deleteIssue(Long id) throws Exception {

    }

    @Override
    public List<String> getCategories(Long pid) throws Exception {
        return null;
    }

    @Override
    public List<User<Long>> getUsers(Long pid) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/user/followers");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                users.add(this.getUser(jsonArray.getJSONObject(i)));
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateUser(User<Long> user) throws Exception {
        return null;
    }

    @Override
    public void deleteUser(Long id) throws Exception {

    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long pid) throws Exception {
        return null;
    }

    @Override
    public CustomField<Long> getCustomField(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateCustomField(CustomField<Long> user) throws Exception {
        return null;
    }

    @Override
    public void deleteCustomField(Long id) throws Exception {

    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new GithubPermissions(this.authentication);
    }

    private User<Long> getUser(JSONObject jsonObject) throws Exception {
        User<Long> user = new User<>();
        user.setId(jsonObject.getLong("id"));
        user.setTitle(jsonObject.getString("login"));
        return user;
    }

    /**
     * @return List of tags
     * @throws Exception
     * @see <a href="https://developer.github.com/v3/issues/labels/">Github Reference</a>
     */
    @Override
    public List<Tag<Long>> getTags() throws Exception {
        return null;
    }

    public void setTitle(String pTitle) {
        this.title = pTitle;
    }
}

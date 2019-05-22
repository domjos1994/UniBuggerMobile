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

package de.domjos.unibuggerlibrary.permissions;

import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.services.engine.Authentication;

public class BugzillaPermissions implements IFunctionImplemented {
    private Authentication authentication;

    public BugzillaPermissions(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public boolean listProjects() {
        return true;
    }

    @Override
    public boolean addProjects() {
        return true;
    }

    @Override
    public boolean updateProjects() {
        return true;
    }

    @Override
    public boolean deleteProjects() {
        return true;
    }

    @Override
    public boolean listVersions() {
        return true;
    }

    @Override
    public boolean addVersions() {
        return true;
    }

    @Override
    public boolean updateVersions() {
        return false;
    }

    @Override
    public boolean deleteVersions() {
        return false;
    }

    @Override
    public boolean listIssues() {
        return false;
    }

    @Override
    public boolean addIssues() {
        return false;
    }

    @Override
    public boolean updateIssues() {
        return false;
    }

    @Override
    public boolean deleteIssues() {
        return false;
    }
}
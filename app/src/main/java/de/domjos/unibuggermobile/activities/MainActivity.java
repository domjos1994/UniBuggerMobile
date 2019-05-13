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

package de.domjos.unibuggermobile.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.SQLiteGeneral;
import de.domjos.unibuggermobile.settings.Globals;
import de.domjos.unibuggermobile.settings.Settings;

public final class MainActivity extends AbstractActivity implements OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ImageView ivMainCover;
    private TextView lblMainCommand;
    private TextView lblAccountTitle;
    private Spinner spMainAccounts;
    private ArrayAdapter<String> accountList;
    private static final int RELOAD_ACCOUNTS = 99;
    public static final Globals globals = new Globals();
    public static Settings settings;

    public MainActivity() {
        super(R.layout.main_activity);
    }

    @Override
    protected void initActions() {
        this.lblMainCommand.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), AccountActivity.class);
            startActivityForResult(intent, MainActivity.RELOAD_ACCOUNTS);
        });

        this.spMainAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = accountList.getItem(position);
                if (item != null) {
                    if (!item.trim().isEmpty()) {
                        Authentication authentication = MainActivity.globals.getSqLiteGeneral().getAccounts("title='" + item + "'").get(0);
                        if (authentication != null) {
                            MainActivity.settings.setCurrentAuthentication(authentication);
                        } else {
                            MainActivity.settings.setCurrentAuthentication(null);
                        }
                    } else {
                        MainActivity.settings.setCurrentAuthentication(null);
                    }
                } else {
                    MainActivity.settings.setCurrentAuthentication(null);
                }

                fillFields();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void initControls() {
        try {
            // init Toolbar
            Toolbar toolbar = this.findViewById(R.id.toolbar);
            this.setSupportActionBar(toolbar);

            // init Drawer-Layout
            this.drawerLayout = this.findViewById(R.id.drawer_layout);

            // init Navigation-View
            NavigationView navigationView = findViewById(R.id.nav_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.drawerLayout, toolbar, R.string.app_name, R.string.app_name);
            this.drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            this.ivMainCover = navigationView.getHeaderView(0).findViewById(R.id.ivMainCover);
            this.lblMainCommand = navigationView.getHeaderView(0).findViewById(R.id.lblMainCommand);
            this.lblAccountTitle = navigationView.getHeaderView(0).findViewById(R.id.lblAccountTitle);
            this.spMainAccounts = navigationView.getHeaderView(0).findViewById(R.id.spMainAccounts);
            this.accountList = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
            this.spMainAccounts.setAdapter(this.accountList);
            this.accountList.notifyDataSetChanged();

            MainActivity.globals.setSqLiteGeneral(new SQLiteGeneral(this.getApplicationContext()));
            this.reloadAccounts();

            MainActivity.settings = new Settings(getApplicationContext());
            Authentication authentication = MainActivity.settings.getCurrentAuthentication();
            if (authentication != null) {
                this.spMainAccounts.setSelection(this.accountList.getPosition(authentication.getTitle()));
            } else {
                this.spMainAccounts.setSelection(this.accountList.getPosition(""));
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, MainActivity.this);
        }
    }

    private void reloadAccounts() {
        this.accountList.clear();
        this.accountList.add("");
        for (Authentication authentication : MainActivity.globals.getSqLiteGeneral().getAccounts("")) {
            this.accountList.add(authentication.getTitle());
        }
    }

    @Override
    public void onActivityResult(int resultCode, int requestCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_ACCOUNTS) {
            this.reloadAccounts();
            this.fillFields();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menSettings:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.navProjects:
                intent = new Intent(this.getApplicationContext(), ProjectActivity.class);
                break;
            default:
                intent = null;
                break;
        }

        if (intent != null) {
            startActivityForResult(intent, 99);
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fillFields() {
        Authentication authentication = MainActivity.settings.getCurrentAuthentication();
        if (authentication != null) {
            if (authentication.getCover() != null) {
                ivMainCover.setImageBitmap(BitmapFactory.decodeByteArray(authentication.getCover(), 0, authentication.getCover().length));
            } else {
                ivMainCover.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp));
            }
            lblAccountTitle.setText(authentication.getTitle());
            lblMainCommand.setText(R.string.accounts_change);
        } else {
            ivMainCover.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp));
            lblAccountTitle.setText(R.string.accounts_noAccount);
            lblMainCommand.setText(R.string.accounts_add);
        }
    }
}

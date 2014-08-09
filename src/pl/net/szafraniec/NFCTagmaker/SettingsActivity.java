/**
 * Copyright (C) 2014 Mateusz Szafraniec
 * This file is part of NFCTagMaker.
 *
 * NFCTagMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * NFCTagMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NFCTagMaker; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Ten plik jest częścią NFCTagMaker.
 *
 * NFCTagMaker jest wolnym oprogramowaniem; możesz go rozprowadzać dalej
 * i/lub modyfikować na warunkach Powszechnej Licencji Publicznej GNU,
 * wydanej przez Fundację Wolnego Oprogramowania - według wersji 2 tej
 * Licencji lub (według twojego wyboru) którejś z późniejszych wersji.
 *
 * Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on
 * użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej
 * gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH
 * ZASTOSOWAŃ. W celu uzyskania bliższych informacji sięgnij do
 * Powszechnej Licencji Publicznej GNU.
 *
 * Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz
 * Powszechnej Licencji Publicznej GNU (GNU General Public License);
 * jeśli nie - napisz do Free Software Foundation, Inc., 59 Temple
 * Place, Fifth Floor, Boston, MA  02110-1301  USA
 */
package pl.net.szafraniec.NFCTagmaker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		EditText uri = (EditText) findViewById(R.id.uri);
		uri.setText(NFCTagmakerSettings.uri);
		EditText name = (EditText) findViewById(R.id.name);
		name.setText(NFCTagmakerSettings.name);
		EditText phone = (EditText) findViewById(R.id.phone);
		phone.setText(NFCTagmakerSettings.phone);
		EditText email = (EditText) findViewById(R.id.email);
		email.setText(NFCTagmakerSettings.email);
		EditText web = (EditText) findViewById(R.id.web);
		web.setText(NFCTagmakerSettings.web);
		EditText im = (EditText) findViewById(R.id.im);
		im.setText(NFCTagmakerSettings.im);
		EditText pgp = (EditText) findViewById(R.id.pgp);
		pgp.setText(NFCTagmakerSettings.pgp);

		Button x = (Button) findViewById(R.id.ok);
		x.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View self) {
				finish();
			}
		});

	}

	@Override
	protected void onStop() {
		super.onStop();
		EditText uri = (EditText) findViewById(R.id.uri);
		NFCTagmakerSettings.uri = uri.getText().toString();
		EditText name = (EditText) findViewById(R.id.name);
		NFCTagmakerSettings.name = name.getText().toString();
		EditText phone = (EditText) findViewById(R.id.phone);
		NFCTagmakerSettings.phone = phone.getText().toString();
		EditText email = (EditText) findViewById(R.id.email);
		NFCTagmakerSettings.email = email.getText().toString();
		EditText web = (EditText) findViewById(R.id.web);
		NFCTagmakerSettings.web = web.getText().toString();
		EditText im = (EditText) findViewById(R.id.im);
		NFCTagmakerSettings.im = im.getText().toString();
		EditText pgp = (EditText) findViewById(R.id.pgp);
		NFCTagmakerSettings.pgp = pgp.getText().toString();
		SharedPreferences settings = getSharedPreferences(
				NFCTagmakerSettings.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("uri", NFCTagmakerSettings.uri);
		editor.putString("name", NFCTagmakerSettings.name);
		editor.putString("phone", NFCTagmakerSettings.phone);
		editor.putString("web", NFCTagmakerSettings.web);
		editor.putString("email", NFCTagmakerSettings.email);
		editor.putString("im", NFCTagmakerSettings.im);
		editor.putString("pgp", NFCTagmakerSettings.pgp);
		editor.commit();
	}

}

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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	final public int ABOUT = 0;
	public static String version;

	private static NdefRecord createNdefMySmartPosterRecord(String text,
			String[] uri, byte[] type) {
		NdefRecord[] records = new NdefRecord[1 + uri.length];
		records[0] = createNdefTextRecord(text);
		for (int i = 1; i < records.length; i++)
			records[i] = createNdefRecord(uri[i - 1], type[i - 1]);
		NdefMessage nm = new NdefMessage(records);
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_SMART_POSTER, new byte[0], nm.toByteArray());
		return record;
	}

	protected static NdefRecord createNdefRecord(String address, byte idCode) {
		byte[] uriField = address.getBytes(Charset.forName("UTF-8"));
		byte[] payload = new byte[uriField.length + 1];
		payload[0] = idCode;
		System.arraycopy(uriField, 0, payload, 1, uriField.length);
		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI,
				new byte[0], payload);
	}

	private static NdefRecord createNdefSmartPosterRecord(String text,
			String[] uri) {
		NdefRecord[] records = new NdefRecord[1 + uri.length];
		records[0] = createNdefTextRecord(text);
		for (int i = 1; i < records.length; i++)
			records[i] = createNdefTelRecord(uri[i - 1]);
		NdefMessage nm = new NdefMessage(records);
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_SMART_POSTER, new byte[0], nm.toByteArray());
		return record;
	}

	protected static NdefRecord createNdefTelRecord(String phone) {
		return createNdefRecord(phone, (byte) 0x05);
	}

	protected static NdefRecord createNdefTextRecord(String text) {
		String lang = "en";
		byte[] textBytes = text.getBytes();
		byte[] langBytes = null;
		try {
			langBytes = lang.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.E(e.getLocalizedMessage());
		}
		int langLength = langBytes.length;
		int textLength = textBytes.length;
		byte[] payload = new byte[1 + langLength + textLength];
		payload[0] = (byte) langLength;
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
				new byte[0], payload);
	}

	protected static NdefRecord createNdefUriRecord(String phone) {
		return createNdefRecord(phone, (byte) 0x00);
	}

	private String getHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			int b = bytes[i] & 0xff;
			if (b < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(b));
			if (i > 0) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private String getMifareType(Tag tag) {
		String type = "Unknown";
		for (String tech : tag.getTechList()) {
			if (tech.equals(MifareClassic.class.getName())) {
				MifareClassic mifareTag = MifareClassic.get(tag);
				switch (mifareTag.getType()) {
				case MifareClassic.TYPE_CLASSIC:
					type = "Classic";
					break;
				case MifareClassic.TYPE_PLUS:
					type = "Plus";
					break;
				case MifareClassic.TYPE_PRO:
					type = "Pro";
					break;
				}
			}

			if (tech.equals(MifareUltralight.class.getName())) {
				MifareUltralight mifareUlTag = MifareUltralight.get(tag);
				switch (mifareUlTag.getType()) {
				case MifareUltralight.TYPE_ULTRALIGHT:
					type = "Ultralight";
					break;
				case MifareUltralight.TYPE_ULTRALIGHT_C:
					type = "Ultralight C";
					break;
				}
			}
		}
		return type;
	}

	private void nfc_disable() {
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
		adapter.disableForegroundDispatch(this);
	}

	private void nfc_enable() {
		// Register for any NFC event (only while we're in the foreground)

		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
		PendingIntent pending_intent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		adapter.enableForegroundDispatch(this, pending_intent, null, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		NfcAdapter Nfc = NfcAdapter.getDefaultAdapter(this);
		if (Nfc == null) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.CantFindNFCAdapter), Toast.LENGTH_LONG)
					.show();
		}
		if (Nfc != null) {
			if (Nfc.isEnabled() != true) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.EnabeNFCFirst), Toast.LENGTH_LONG)
						.show();
				startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
			}
		}
		SharedPreferences settings = getSharedPreferences(
				NFCTagmakerSettings.PREFS_NAME, 0);
		NFCTagmakerSettings.uri = settings.getString("uri",
				getString(R.string.defaultUri));
		NFCTagmakerSettings.phone = settings.getString("phone",
				getString(R.string.defaultPhone));
		NFCTagmakerSettings.name = settings.getString("name",
				getString(R.string.defaultName));
		NFCTagmakerSettings.email = settings.getString("email",
				getString(R.string.defaultEmail));
		NFCTagmakerSettings.web = settings.getString("web",
				getString(R.string.defaultWeb));
		NFCTagmakerSettings.im = settings.getString("im",
				getString(R.string.defaultIM));
		NFCTagmakerSettings.pgp = settings.getString("pgp",
				getString(R.string.defaultPGP));

		Intent intent = getIntent();
		String action = intent.getAction();
		if (action.equalsIgnoreCase(Intent.ACTION_SEND)
				&& intent.hasExtra(Intent.EXTRA_TEXT)) {
			String s = intent.getStringExtra(Intent.EXTRA_TEXT);
			NFCTagmakerSettings.uri = s;
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("uri", NFCTagmakerSettings.uri);
			editor.commit();
		}
		Button x = (Button) findViewById(R.id.quit);
		x.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View self) {
				finish();
			}
		});

		Button advanced = (Button) findViewById(R.id.advanced);
		advanced.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View self) {
				Intent intent = new Intent(getApplicationContext(),
						AdvancedActivity.class);
				startActivity(intent);
			}
		});

		Button wu = (Button) findViewById(R.id.Writeuri);
		wu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View self) {
				if (NFCTagmakerSettings.uri.length() != 0) {
					NdefRecord ndef_records = NdefRecord
							.createUri(NFCTagmakerSettings.uri);
					NFCTagmakerSettings.nfc_payload = new NdefMessage(
							ndef_records);
					Intent intent = new Intent(getApplicationContext(),
							WriteNFCActivity.class);
					startActivity(intent);
				} else {
					Intent settings = new Intent(getApplicationContext(),
							SettingsActivity.class);
					startActivity(settings);
				}
			}
		});

		Button wp = (Button) findViewById(R.id.Writephone);
		wp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View self) {
				if ((NFCTagmakerSettings.phone.length() != 0)
						&& (NFCTagmakerSettings.name.length() != 0)) {
					NdefRecord[] ndef_name = new NdefRecord[1];
					String[] uri = new String[] { NFCTagmakerSettings.phone };
					ndef_name[0] = createNdefSmartPosterRecord(
							NFCTagmakerSettings.name, uri);
					NFCTagmakerSettings.nfc_payload = new NdefMessage(ndef_name);
					Intent intent = new Intent(getApplicationContext(),
							WriteNFCActivity.class);
					startActivity(intent);
				} else {
					Intent settings = new Intent(getApplicationContext(),
							SettingsActivity.class);
					startActivity(settings);
				}
			}
		});

		Button sp = (Button) findViewById(R.id.Writesp);
		sp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View self) {
				NdefRecord[] ndef_name = new NdefRecord[1];
				int ile = 0;
				int gdzie = 0;

				if (NFCTagmakerSettings.phone.length() > 3) {
					ile = ile + 1;
				}

				if (NFCTagmakerSettings.uri.length() > 3) {
					ile = ile + 1;
				}

				if (NFCTagmakerSettings.email.length() > 6) {
					ile = ile + 1;
				}
				if (NFCTagmakerSettings.web.length() > 6) {
					ile = ile + 1;
				}

				String[] uri = new String[ile];
				byte[] type = new byte[ile];

				if (NFCTagmakerSettings.phone.length() > 3) {
					uri[gdzie] = NFCTagmakerSettings.phone;
					type[gdzie] = 0x05;
					gdzie = gdzie + 1;
				}

				if (NFCTagmakerSettings.uri.length() > 3) {
					uri[gdzie] = NFCTagmakerSettings.uri;
					type[gdzie] = 0x00;
					gdzie = gdzie + 1;
				}

				if (NFCTagmakerSettings.email.length() > 6) {
					uri[gdzie] = NFCTagmakerSettings.email;
					type[gdzie] = 0x06;
					gdzie = gdzie + 1;
				}
				if (NFCTagmakerSettings.web.length() > 6) {
					uri[gdzie] = NFCTagmakerSettings.web;
					type[gdzie] = 0x00;
					gdzie = gdzie + 1;
				}
				ndef_name[0] = createNdefMySmartPosterRecord(
						NFCTagmakerSettings.name, uri, type);
				NFCTagmakerSettings.nfc_payload = new NdefMessage(ndef_name);
				Intent intent = new Intent(getApplicationContext(),
						WriteNFCActivity.class);
				startActivity(intent);
			}
		});

		Button wvc = (Button) findViewById(R.id.WritevCard);
		wvc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View self) {
				String vcardcontent = "BEGIN:VCARD\r\nVERSION:3.0\r\nN:"
						+ NFCTagmakerSettings.name + "\r\n";

				if (NFCTagmakerSettings.phone.length() > 3) {
					vcardcontent = vcardcontent + "TEL;TYPE=cell:"
							+ NFCTagmakerSettings.phone + "\r\n";
				}

				if (NFCTagmakerSettings.email.length() > 6) {
					vcardcontent = vcardcontent + "EMAIL:"
							+ NFCTagmakerSettings.email + "\r\n";
				}

				if (NFCTagmakerSettings.web.length() > 6) {
					vcardcontent = vcardcontent + "URL:"
							+ NFCTagmakerSettings.web + "\r\n";
				}

				if (NFCTagmakerSettings.im.length() > 6) {
					vcardcontent = vcardcontent + "IMPP:"
							+ NFCTagmakerSettings.im + "\r\n";
				}

				if (NFCTagmakerSettings.pgp.length() > 6) {
					vcardcontent = vcardcontent + "KEY;TYPE=PGP:"
							+ NFCTagmakerSettings.pgp + "\r\n";
				}

				vcardcontent = vcardcontent + "END:VCARD\r\n";
				NdefRecord ndef_records = NdefRecord.createMime("text/x-vCard",
						vcardcontent.getBytes());
				NFCTagmakerSettings.nfc_payload = new NdefMessage(ndef_records);
				Intent intent = new Intent(getApplicationContext(),
						WriteNFCActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onNewIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String typ_karty = getMifareType(tag);
			Toast.makeText(getApplicationContext(),
					getString(R.string.card_type) + typ_karty,
					Toast.LENGTH_LONG).show();
			Ndef ndef = Ndef.get(tag);
			Toast.makeText(getApplicationContext(),
					"ID:" + getHex(tag.getId()), Toast.LENGTH_LONG).show();
			if (ndef != null) {
				Toast.makeText(getApplicationContext(),
						"Type:" + ndef.getType() + "size:" + ndef.getMaxSize(),
						Toast.LENGTH_LONG).show();
			}
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(100);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.ABOUT:
			AboutDialog about = new AboutDialog(this);
			about.setTitle(getString(R.string.About));
			about.show();
			break;
		case R.id.settings:
			Intent settings = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(settings);
			break;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		nfc_disable();
	}

	@Override
	protected void onResume() {
		super.onResume();
		nfc_enable();
	}
}

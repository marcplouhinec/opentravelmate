package org.opentravelmate.launcher.window.controller;

import org.opentravelmate.launcher.window.WindowActivity;

public class DefaultWindowController implements WindowController {
	
	public DefaultWindowController(WindowActivity windowActivity, WindowOptions options) {
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
	}
	
	public static class WindowOptions {
		
		public static WindowOptions fromJson(String jsonWindowOptions) {
			return null;
		}
	}

}

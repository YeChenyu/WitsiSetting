/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.witsi.settings.display_media;

import com.witsi.views.MyAlertDialog;
import com.wtisi.settings.R;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

public class WarnedListPreference extends ListPreference {
	
    public WarnedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        // Ignore this until an explicit call to click()
    }

    public void click() {
        super.onClick();
    }
    
//    @Override
//    protected void showDialog(Bundle state) {
//    	// TODO Auto-generated method stub
//    	View view = onCreateDialogView();
//    	if(view == null){
//    		onBindDialogView(view);
//    	}
//    	
//    	MyAlertDialog dialog = new MyAlertDialog(getContext());
//    	dialog.setTitle(R.string.display_settings_title);
//    	dialog.setView(view);
//    	dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface arg0, int arg1) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//    	dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//    	dialog.show();
//    }
}


/*
 * Copyright (C) 2014 The Retro Band - Open source smart band project
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

package com.hardcopy.retroband.fragments;

public interface IDialogListener {
	public static final int CALLBACK_ENABLE_MESSAGE = 1;
	public static final int CALLBACK_ENABLE_PACKAGE = 2;
	
	public static final int CALLBACK_CLOSE = 1000;
	
	public void OnDialogCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4);
}

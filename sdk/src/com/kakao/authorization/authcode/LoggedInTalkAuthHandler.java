/**
 * Copyright 2014 Daum Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.authorization.authcode;

import android.content.Intent;
import com.kakao.helper.TalkProtocol;

/**
 * Talk에 로그인 되어 있는 계정을 이용하여 authorization code를 받는다.
 * @author MJ
 */
public class LoggedInTalkAuthHandler extends AuthorizationCodeHandler {

    public LoggedInTalkAuthHandler(final GetterAuthorizationCode authorizer) {
        super(authorizer);
    }

    @Override
    protected Intent getIntent(AuthorizationCodeRequest request) {
        return TalkProtocol.createLoggedInActivityIntent(authorizer.getApplicationContext(),
            request.getAppKey(), request.getRedirectURI(), request.getExtras(),
            needProjectLogin(request.getExtras()));
    }
}

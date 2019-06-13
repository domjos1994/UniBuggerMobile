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

package de.domjos.unibuggerlibrary.services.engine;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.net.Proxy;

public class SoapEngine {
    private Authentication authentication;
    protected final String soapPath;


    public SoapEngine(Authentication authentication, String path) {
        this.authentication = authentication;
        this.soapPath = this.authentication.getServer() + path;
    }

    protected Object executeAction(SoapObject request, String action, boolean login) throws Exception {

        if (login) {
            if (this.authentication.getAPIKey().isEmpty()) {
                request.addProperty("username", this.authentication.getUserName());
                request.addProperty("password", this.authentication.getPassword());
            } else {
                request.addProperty("username", this.authentication.getAPIKey());
                request.addProperty("password", "");
            }
        }

        SoapSerializationEnvelope envelope = this.getEnvelope(request);
        HttpTransportSE transportSE = this.getHttpTransportSE();
        // initialize MarshalBase for sending files
        new MarshalBase64().register(envelope);
        transportSE.call(this.soapPath + "/" + action, envelope);
        return envelope.getResponse();
    }

    private SoapSerializationEnvelope getEnvelope(SoapObject request) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.encodingStyle = SoapEnvelope.ENC;
        envelope.setOutputSoapObject(request);
        return envelope;
    }

    private HttpTransportSE getHttpTransportSE() {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY, this.soapPath, 60000);
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }
}

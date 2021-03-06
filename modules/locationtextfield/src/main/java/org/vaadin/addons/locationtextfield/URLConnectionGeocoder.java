/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.vaadin.addons.locationtextfield;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link LocationProvider} shell implementation that fetches data from an URL and delegates to implementing class to decode
 */
public abstract class URLConnectionGeocoder implements LocationProvider {

    public Collection<GeocodedLocation> geocode(String address) throws GeocodingException {
        final Set<GeocodedLocation> locations = new LinkedHashSet<GeocodedLocation>();
        BufferedReader reader = null;
        try {
            String addr = getURL(address);
            URLConnection con = new URL(addr).openConnection();
            con.setDoOutput(true);
            con.connect();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream(), getEncoding()));
            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            locations.addAll(createLocations(address, builder.toString()));
        } catch (Exception e) {
            throw new GeocodingException(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return locations;
    }

    /**
     * Encoding the response stream is to be expected.  Default is UTF-8.  Override in subclass as necessary.
     */
    protected String getEncoding() {
        return "UTF-8";
    }

    /**
     * Retrieve the full URL to fetch
     * @param address input address
     * @return full URL
     * @throws UnsupportedEncodingException if subclass uses {@link java.net.URLEncoder} and it fails
     */
    protected abstract String getURL(String address) throws UnsupportedEncodingException;

    /**
     * Creates {@link GeocodedLocation} objects from response stream
     * @param address input address
     * @param input response stream as string
     * @return collection of {@link GeocodedLocation} objects
     * @throws GeocodingException
     */
    protected abstract Collection<? extends GeocodedLocation> createLocations(String address, String input)
      throws GeocodingException;
}

/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015-2019 javafx.ninja <info@javafx.ninja>

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.

*/

package ninja.javafx.smartcsv.validation.configuration;

import com.google.gson.annotations.SerializedName;

/**
 * Types of JSON Table Schema
 * @see <a href="http://specs.frictionlessdata.io/json-table-schema/">JSON Table Schema</a>
 */
public enum Type {
    @SerializedName("string")STRING,
    @SerializedName("integer")INTEGER,
    @SerializedName("number")NUMBER,
    @SerializedName("date")DATE,
    @SerializedName("datetime")DATETIME,
    @SerializedName("time")TIME
// TODO: currently not supported
//        @SerializedName("object") OBJECT,
//        @SerializedName("array") ARRAY,
//        @SerializedName("duration") DURATION,
//        @SerializedName("geopoint") GEOPOINT,
//        @SerializedName("geojson") GEOJSON
}

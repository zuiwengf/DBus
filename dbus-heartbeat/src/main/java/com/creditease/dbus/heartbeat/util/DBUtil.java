/*-
 * <<
 * DBus
 * ==
 * Copyright (C) 2016 - 2018 Bridata
 * ==
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
 * >>
 */

package com.creditease.dbus.heartbeat.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.creditease.dbus.heartbeat.log.LoggerFactory;

public class DBUtil {

    private DBUtil() {
    }

    public static void close(Object obj) {
        if (obj == null)
            return;
        try {
            if (obj instanceof PreparedStatement)
                ((PreparedStatement) obj).close();
            if (obj instanceof ResultSet)
                ((ResultSet) obj).close();
            if (obj instanceof Connection)
                ((Connection) obj).close();
        } catch (SQLException e) {
            LoggerFactory.getLogger().error("[db-close-error]", e);
        }
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.audit.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.forgerock.json.fluent.JsonNode;
import org.forgerock.json.fluent.JsonNodeException;

import org.forgerock.openidm.audit.AuditService;
import org.forgerock.openidm.config.InvalidException;
import org.forgerock.openidm.objset.BadRequestException;
import org.forgerock.openidm.objset.ConflictException;
import org.forgerock.openidm.objset.ForbiddenException;
import org.forgerock.openidm.objset.NotFoundException;
import org.forgerock.openidm.objset.ObjectSet;
import org.forgerock.openidm.objset.ObjectSetException;
import org.forgerock.openidm.objset.PreconditionFailedException;
import org.forgerock.openidm.objset.Patch;
import org.forgerock.openidm.objset.PreconditionFailedException;

/**
 * Comma delimited audit logger
 * @author aegloff
 */
public class CSVAuditLogger implements AuditLogger {
    final static Logger logger = LoggerFactory.getLogger(CSVAuditLogger.class);

    public final static String CONFIG_LOG_LOCATION = "location";
    
    File auditLogDir;

    public void setConfig(Map config) throws InvalidException {
        String location = null;
        try {
            location = (String) config.get(CONFIG_LOG_LOCATION);
            auditLogDir = new File(location);
            auditLogDir.mkdirs();
        } catch (Exception ex) {
            throw new InvalidException("Configuration CVS file location must be a directory and '" + location 
                    + "' is invalid " + ex.getMessage(), ex);
        }
    }
    
    public void cleanup() {
        
    }
    
    /**
     * {@inheritdoc}
     */
    public Map<String, Object> read(String fullId) throws ObjectSetException {
        // TODO
        return new HashMap();
    }

    /**
     * {@inheritdoc}
     */
    public void create(String fullId, Map<String, Object> obj) throws ObjectSetException {
        String type = getObjectType(fullId);
        String lineSep = System.getProperty("line.separator");
        
        // TODO: optimize buffered, cached writing
        FileWriter fileWriter = null;
        try {
            // TODO: Optimize ordering etc.
            Collection<String> fieldOrder = 
                new TreeSet<String>(Collator.getInstance());
            fieldOrder.addAll(obj.keySet());
            
            File auditFile = new File(auditLogDir, type + ".csv");
            if (!auditFile.exists()) {
                synchronized (this) {
                    boolean created = auditFile.createNewFile();
                    fileWriter = new FileWriter(auditFile, true);
                    if (created) {                        
                        writeHeaders(fieldOrder, fileWriter, lineSep);
                    }
                }
            } else {
                fileWriter = new FileWriter(auditFile, true);
            }

            String key = null;
            Iterator iter = fieldOrder.iterator();
            while(iter.hasNext()) {
                key = (String) iter.next();
                Object value = obj.get(key);
                fileWriter.append("\"");
                if (value != null) {
                    String rawStr = value.toString();
                    // Escape quotes with double quotes
                    String escapedStr = rawStr.replaceAll("\"", "\"\"");
                    fileWriter.append(escapedStr);
                }
                fileWriter.append("\"");
                if (iter.hasNext()) {
                    fileWriter.append(",");
                }
            }
            fileWriter.append(lineSep);
        } catch (Exception ex) {
            throw new BadRequestException(ex);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception ex) {
                // Quietly try the close
            } 
        }
    }
    
    private void writeHeaders(Collection<String> fieldOrder, FileWriter fileWriter, String lineSep) 
            throws IOException {
        Iterator iter = fieldOrder.iterator();
        while(iter.hasNext()) {
            String key = (String) iter.next();
            fileWriter.append("\"");
            String escapedStr = key.replaceAll("\"", "\"\"");
            fileWriter.append(escapedStr);
            fileWriter.append("\"");
            if (iter.hasNext()) {
                fileWriter.append(",");
            }
        }
        fileWriter.append(lineSep);
    }

    
    /**
     * Audit service does not support changing audit entries.
     */
    public void update(String fullId, String rev, Map<String, Object> obj) throws ObjectSetException {
        throw new UnsupportedOperationException();
    }

    /**
     * Audit service currently does not support deleting audit entries.
     */ 
    public void delete(String fullId, String rev) throws ObjectSetException {
        throw new UnsupportedOperationException();
    }

    /**
     * Audit service does not support changing audit entries.
     */
    public void patch(String id, String rev, Patch patch) throws ObjectSetException {
        throw new UnsupportedOperationException();
    }

    /**
     * Currently not supported.
     * 
     * {@inheritdoc}
     */
    public Map<String, Object> query(String fullId, Map<String, Object> params) throws ObjectSetException {
        // TODO
        return new HashMap();
    }
    
    // TODO: replace with common utility to handle ID, this is temporary
    private String getObjectType(String id) {
        String type = null;
        int lastSlashPos = id.lastIndexOf("/");
        if (lastSlashPos > -1) {
            int startPos = 0;
            // This should not be necessary as relative URI should not start with slash
            if (id.startsWith("/")) {
                startPos = 1;
            }
            type = id.substring(startPos, lastSlashPos);
            logger.trace("Full id: {} Extracted type: {}", id, type);
        }
        return type;
    }
}
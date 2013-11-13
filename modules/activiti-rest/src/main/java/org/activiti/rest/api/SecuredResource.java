/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.activiti.rest.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.rest.application.ActivitiRestApplication;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class SecuredResource extends ServerResource {

  protected static final String USER = "user";
  protected static final String ADMIN = "admin";
  
  protected String loggedInUser;
  
  protected boolean authenticate() {
    return authenticate(null);
  }
  
  protected boolean authenticate(String group) {
    loggedInUser = ((ActivitiRestApplication) getApplication()).authenticate(getRequest(), getResponse());
    if(loggedInUser == null) {
      // Not authenticated
      setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Authentication is required");
      return false;
    
    } else if(group == null) {
      ActivitiUtil.getIdentityService().setAuthenticatedUserId(loggedInUser);
      return true;
    
    } else {
      boolean allowed = false;
      List<Group> groupList = ActivitiUtil.getIdentityService().createGroupQuery().groupMember(loggedInUser).list();
      if(groupList != null) {
        for (Group groupObject : groupList) {
          if(groupObject.getId().equals(group)) {
            allowed = true;
            ActivitiUtil.getIdentityService().setAuthenticatedUserId(loggedInUser);
            break;
          }
        }
      }
      if(allowed == false) {
        setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User is not part of the group " + group);
      }
      return allowed;
    }
  }
  
  protected Map<String, Object> retrieveVariables(JsonNode jsonNode) {
    Map<String, Object> variables = new HashMap<String, Object>();
    if (jsonNode != null) {
      Iterator<String> itName = jsonNode.getFieldNames();
      while(itName.hasNext()) {
        String name = itName.next();
        JsonNode valueNode = jsonNode.path(name);
        if (valueNode.isBoolean()) {
          variables.put(name, valueNode.getBooleanValue());
        } else if (valueNode.isInt()) {
          variables.put(name, valueNode.getIntValue());
        } else if (valueNode.isLong()) {
          variables.put(name, valueNode.getLongValue());
        } else if (valueNode.isDouble()) {
          variables.put(name, valueNode.getDoubleValue());
        } else if (valueNode.isTextual()) {
          variables.put(name, valueNode.getTextValue());
        } else {
          variables.put(name, valueNode.getValueAsText());
        }
      }
    }
    return variables;
  }

  protected String getQueryParameter(String name, Form query) {
      return query.getFirstValue(name);
    }
    
    /**
     * @return the value for the given query-parameter name, as an integer value. 
     * Returns null, if the query-parameter was not set.
     * 
     * @throws ActivitiIllegalArgumentException when the query parameter is set but has cannot be converted to an integer
     */
    protected Integer getQueryParameterAsInt(String name, Form query) {
      Integer result = null;
      String stringValue = getQueryParameter(name, query);
      if(stringValue != null) {
        try {
          result = Integer.parseInt(stringValue);
        } catch(NumberFormatException nfe) {
          throw new ActivitiIllegalArgumentException("The given value for query-parameter '" + name + "' is not an integer: " + stringValue);
        }
      }
      return result;
    }
    
    /**
     * @return the value for the given query-parameter name, as an boolean value. 
     * Returns null, if the query-parameter was not set.
     * 
     * @throws ActivitiIllegalArgumentException when the query parameter is set but has cannot be converted to a boolean
     */
    protected Boolean getQueryParameterAsBoolean(String name, Form query) {
      String stringValue = getQueryParameter(name, query);
      if(stringValue != null) {
        if(Boolean.TRUE.toString().equals(stringValue.toLowerCase())) {
          return Boolean.TRUE;
        } else if(Boolean.FALSE.toString().equals(stringValue.toLowerCase())) {
          return Boolean.FALSE;
        } else {
          throw new ActivitiIllegalArgumentException("The given value for query-parameter '" + name + "' should be one fo 'true' or 'false', instead of: " + stringValue);
        }
      }
      
      return null;
    }
}

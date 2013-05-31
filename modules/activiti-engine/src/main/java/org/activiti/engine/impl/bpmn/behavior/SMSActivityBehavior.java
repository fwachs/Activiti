package org.activiti.engine.impl.bpmn.behavior;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.restlet.resource.ClientResource;

public class SMSActivityBehavior implements JavaDelegate {

    private static Logger logger = Logger.getLogger(SMSActivityBehavior.class
            .getName());

    protected Expression username;
    protected Expression password;
    protected Expression to;
    protected Expression from;
    protected Expression text;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String to = getStringFromField(this.to, execution);
        try {
            Object revisionObject = execution.getVariable("jobRevision");
            if (revisionObject != null) {
                int revision = Integer.parseInt(revisionObject.toString());
                logger.log(Level.SEVERE, "###revision###sms###" + revision + " "
                        + to);
                if (revision > 1) {
                    return;
                }
            }            
        } catch(Exception e) {
            logger.log(Level.SEVERE, "###revision### couldn't parse: " + execution.getVariable("jobRevision"), e);
        }
        String from = getStringFromField(this.from, execution);
        String text = getStringFromField(this.text, execution);
        String username = getStringFromField(this.username, execution);
        String password = getStringFromField(this.password, execution);

        ClientResource cr = new ClientResource(
                "http://api.infobip.com/api/v2/sendsms/plain");
        cr.getReference().addQueryParameter("username", username);
        cr.getReference().addQueryParameter("password", password);
        cr.getReference().addQueryParameter("sender", from);
        cr.getReference().addQueryParameter("smstext", text);
        cr.getReference().addQueryParameter("GSM", to);
        logger.info("Sending SMS to: " + to);
        cr.get();
    }

    protected String getStringFromField(Expression expression,
            DelegateExecution execution) {
        if (expression != null) {
            Object value = expression.getValue(execution);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

}

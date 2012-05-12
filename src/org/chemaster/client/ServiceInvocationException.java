/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chemaster.client;

/**
 *
 * @author chung
 */
public class ServiceInvocationException extends Exception {

    public ServiceInvocationException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ServiceInvocationException(String string) {
        super(string);
    }

    public ServiceInvocationException() {
    }
}

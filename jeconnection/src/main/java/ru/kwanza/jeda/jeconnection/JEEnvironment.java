package ru.kwanza.jeda.jeconnection;

import com.sleepycat.je.*;
import com.sleepycat.je.log.LogUtils;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.File;

/**
 * @author Guzanov Alexander
 */
public class JEEnvironment extends XAEnvironment {

    private final File envHome;

    public JEEnvironment(File envHome, EnvironmentConfig configuration) throws EnvironmentNotFoundException, EnvironmentLockedException {
        super(envHome, configuration);
        this.envHome = envHome;
    }

    @Override
    public Transaction getXATransaction(Xid xid) {
        return super.getXATransaction(getXid(xid));
    }

    @Override
    public void setXATransaction(Xid xid, Transaction txn) {
        super.setXATransaction(getXid(xid), txn);
    }

    private Xid getXid(Xid xid) {
        return (xid instanceof LogUtils.XidImpl) ?
                xid :
                new LogUtils.XidImpl(xid.getFormatId(), xid.getGlobalTransactionId(), xid.getBranchQualifier());
    }

    @Override
    public void commit(Xid xid, boolean ignore) throws XAException {
        super.commit(getXid(xid), ignore);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        super.end(getXid(xid), flags);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        super.forget(getXid(xid));
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return super.prepare(getXid(xid));
    }

    @Override
    public Xid[] recover(int flags) throws XAException {
        return super.recover(flags);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        super.rollback(getXid(xid));
    }


    @Override
    public void start(Xid xid, int flags) throws XAException {
        super.start(getXid(xid), flags);
    }

    @Override
    public boolean isSameRM(XAResource rm) throws XAException {
        if (rm == this && envImpl == null) {
            throw new XAException("Resource closed!");
        }

        if (envImpl == null) {
            return false;
        }

        final JEEnvironment ev = (JEEnvironment) rm;

        if(envHome.equals(ev.envHome)){
            return true;
        }

        return false;

    }

    @Override
    public String toString() {
        return "JEEnvironment-" + hashCode();
    }
}

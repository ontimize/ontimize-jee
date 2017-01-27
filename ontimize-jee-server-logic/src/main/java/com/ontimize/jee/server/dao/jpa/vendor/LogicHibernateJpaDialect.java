/**
 *
 */
package com.ontimize.jee.server.dao.jpa.vendor;

import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

import com.autobizlogic.abl.engine.ConstraintException;
import com.autobizlogic.abl.engine.ConstraintFailure;

/**
 * The Class LogicHibernateJpaDialect.
 */
public class LogicHibernateJpaDialect extends HibernateJpaDialect {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7990368933532177247L;

    /**
     * @see org.springframework.orm.jpa.vendor.HibernateJpaDialect#convertHibernateAccessException(org.hibernate.HibernateException)
     */
    @Override
	protected DataAccessException convertHibernateAccessException(HibernateException ex) {
		if (ex instanceof ConstraintException) {
			StringBuffer buffer = new StringBuffer();
			ConstraintException cex = (ConstraintException) ex;
			List<ConstraintFailure> failures = cex.getConstraintFailures();
			if (failures.size() >= 1){
				buffer.append(failures.get(0).getConstraintMessage());
			}
			return new ConstraintLogicException(buffer.toString());
		}
		return super.convertHibernateAccessException(ex);
	}
}

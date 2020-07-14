package com.ontimize.jee.server.services.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ontimize.jee.common.services.session.ISessionService;

/**
 * The Class SessionServiceImpl.
 */
@Service("SessionService")
@Lazy(value = true)
public class SessionServiceImpl implements ISessionService {

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.session.ISessionService#closeSession()
     */
    @Override
    public void closeSession() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}

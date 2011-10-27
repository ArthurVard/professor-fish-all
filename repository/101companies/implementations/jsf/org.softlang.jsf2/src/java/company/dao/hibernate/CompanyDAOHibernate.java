package company.dao.hibernate;

import company.dao.hibernate.generic.GenericHibernateDAO;
import company.dao.interfaces.CompanyDAO;
import company.hibernate.mapping.Company;

/**
 *
 * @author Tobias
 */
public class CompanyDAOHibernate
        extends GenericHibernateDAO<Company, Long>
        implements CompanyDAO {
    
}

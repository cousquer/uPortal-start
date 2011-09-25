/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.permission.dao.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JpaPermissionOwnerDao provides a default JPA/Hibernate implementation of
 * the IPermissionOwnerDao interface.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Repository("permissionOwnerDao")
public class JpaPermissionOwnerDao extends BasePortalJpaDao implements IPermissionOwnerDao {
    private static final String FIND_ALL_PERMISSION_OWNERS_CACHE_REGION = PermissionOwnerImpl.class.getName() + ".query.FIND_ALL_PERMISSION_OWNERS";
    private static final String FIND_PERMISSION_OWNER_BY_FNAME_CACHE_REGION = PermissionOwnerImpl.class.getName() + ".query.FIND_PERMISSION_OWNER_BY_FNAME";
    
    protected final Log log = LogFactory.getLog(getClass());
    
    private CriteriaQuery<PermissionOwnerImpl> findAllPermissionOwners;

    @Override
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.findAllPermissionOwners = this.buildFindAllPermissionOwners(cb);
    }

    protected CriteriaQuery<PermissionOwnerImpl> buildFindAllPermissionOwners(final CriteriaBuilder cb) {
        final CriteriaQuery<PermissionOwnerImpl> criteriaQuery = cb.createQuery(PermissionOwnerImpl.class);
        final Root<PermissionOwnerImpl> ownerRoot = criteriaQuery.from(PermissionOwnerImpl.class);
        criteriaQuery.select(ownerRoot);
        
        return criteriaQuery;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jasig.portal.permissions.dao.IPermissionOwnerDao#getAllPermissible()
     */
    @Override
    public List<IPermissionOwner> getAllPermissionOwners() {
        final TypedQuery<PermissionOwnerImpl> query = this.createQuery(this.findAllPermissionOwners, FIND_ALL_PERMISSION_OWNERS_CACHE_REGION);
        
        final List<PermissionOwnerImpl> resultList = query.getResultList();
        return new ArrayList<IPermissionOwner>(resultList);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getOrCreatePermissionOwner(java.lang.String)
     */
    @Override
    @Transactional
    public IPermissionOwner getOrCreatePermissionOwner(String name, String fname) {
        IPermissionOwner owner = getPermissionOwner(fname);
        if (owner == null) {
            owner = new PermissionOwnerImpl(name, fname);
            this.entityManager.persist(owner);
        }
        return owner;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionOwner(java.lang.Long)
     */
    @Override
    public IPermissionOwner getPermissionOwner(long id){
        return entityManager.find(PermissionOwnerImpl.class, id);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionOwner(java.lang.String)
     */
    @Override
    public IPermissionOwner getPermissionOwner(String fname) {
        final NaturalIdQueryBuilder<PermissionOwnerImpl> naturalIdQuery = this.createNaturalIdQuery(PermissionOwnerImpl.class, FIND_PERMISSION_OWNER_BY_FNAME_CACHE_REGION);
        naturalIdQuery.setNaturalIdParam(PermissionOwnerImpl_.fname, fname);
        
        return naturalIdQuery.execute();
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#saveOwner(org.jasig.portal.permission.IPermissionOwner)
     */
    @Override
    @Transactional
    public IPermissionOwner saveOwner(IPermissionOwner owner) {
        this.entityManager.persist(owner);
        return owner;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getOrCreatePermissionActivity(org.jasig.portal.permission.IPermissionOwner, java.lang.String)
     */
    @Override
    @Transactional
    public IPermissionActivity getOrCreatePermissionActivity(
            IPermissionOwner owner, String name, String fname, String targetProviderKey) {
        IPermissionActivity activity = getPermissionActivity(owner.getId(), fname);
        if (activity == null) {
            activity = new PermissionActivityImpl(name, fname, targetProviderKey);
            owner.getActivities().add(activity);
        }
        return activity;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.Long)
     */
    @Override
    public IPermissionActivity getPermissionActivity(long id) {
        return entityManager.find(PermissionActivityImpl.class, id);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.Long, java.lang.String)
     */
    @Override
    public IPermissionActivity getPermissionActivity(long ownerId, String activityFname) {
        final IPermissionOwner permissionOwner = this.getPermissionOwner(ownerId);
        return findActivity(permissionOwner, activityFname);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.String, java.lang.String)
     */
    @Override
    public IPermissionActivity getPermissionActivity(String ownerFname, String activityFname) {
        final IPermissionOwner permissionOwner = this.getPermissionOwner(ownerFname);
        return findActivity(permissionOwner, activityFname);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#savePermissionActivity(org.jasig.portal.permission.IPermissionActivity)
     */
    @Override
    @Transactional
    public IPermissionActivity savePermissionActivity(IPermissionActivity activity) {
        this.entityManager.persist(activity);
        return activity;
    }


    protected IPermissionActivity findActivity(final IPermissionOwner permissionOwner, String activityFname) {
        if (permissionOwner == null) {
            return null;
        }
        
        final Set<IPermissionActivity> activities = permissionOwner.getActivities();
        for (final IPermissionActivity permissionActivity : activities) {
            if (activityFname.equals(permissionActivity.getFname())) {
                return permissionActivity;
            }
        }
        
        return null;
    }
}

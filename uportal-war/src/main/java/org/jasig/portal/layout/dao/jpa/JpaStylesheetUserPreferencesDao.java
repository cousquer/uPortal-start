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

package org.jasig.portal.layout.dao.jpa;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.security.IPerson;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of {@link IStylesheetUserPreferencesDao}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository("stylesheetUserPreferencesDao")
public class JpaStylesheetUserPreferencesDao extends BasePortalJpaDao implements IStylesheetUserPreferencesDao {
    private static final String FIND_ALL_PREFERENCES_CACHE_REGION = StylesheetUserPreferencesImpl.class.getName() + ".query.FIND_ALL_PREFERENCES";
    private static final String FIND_PREFERENCES_BY_DESCRIPTOR_PERSON_PROFILE_CACHE_REGION = StylesheetUserPreferencesImpl.class.getName() + ".query.FIND_PREFERENCES_BY_DESCRIPTOR_PERSON_PROFILE_CACHE_REGION";
    
    private CriteriaQuery<StylesheetUserPreferencesImpl> findAllPreferences;
    
    @Override
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.findAllPreferences = this.buildFindAllPreferences(cb);
    }
    
    protected CriteriaQuery<StylesheetUserPreferencesImpl> buildFindAllPreferences(final CriteriaBuilder cb) {
        final CriteriaQuery<StylesheetUserPreferencesImpl> criteriaQuery = cb.createQuery(StylesheetUserPreferencesImpl.class);
        final Root<StylesheetUserPreferencesImpl> descriptorRoot = criteriaQuery.from(StylesheetUserPreferencesImpl.class);
        criteriaQuery.select(descriptorRoot);
        
        return criteriaQuery;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#createStylesheetUserPreferences(org.jasig.portal.layout.om.IStylesheetDescriptor, org.jasig.portal.security.IPerson, org.jasig.portal.UserProfile)
     */
    @Transactional
    @Override
    public IStylesheetUserPreferences createStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile) {
        final int userId = person.getID();
        final int profileId = profile.getProfileId();
        final StylesheetUserPreferencesImpl stylesheetUserPreferences = new StylesheetUserPreferencesImpl(stylesheetDescriptor, userId, profileId);
        
        this.entityManager.persist(stylesheetUserPreferences);
        
        return stylesheetUserPreferences;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#getStylesheetUserPreferences()
     */
    @Override
    public List<? extends IStylesheetUserPreferences> getStylesheetUserPreferences() {
        final TypedQuery<StylesheetUserPreferencesImpl> query = this.createQuery(this.findAllPreferences, FIND_ALL_PREFERENCES_CACHE_REGION);
        return query.getResultList();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#getStylesheetUserPreferences(int)
     */
    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(long id) {
        return this.entityManager.find(StylesheetUserPreferencesImpl.class, id);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#getStylesheetUserPreferences(long, org.jasig.portal.security.IPerson, org.jasig.portal.UserProfile)
     */
    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile) {
        return this.getStylesheetUserPreferences(stylesheetDescriptor, person.getID(), profile.getProfileId());
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, int personId, int profileId) {
        final NaturalIdQueryBuilder<StylesheetUserPreferencesImpl> naturalIdQuery = this.createNaturalIdQuery(StylesheetUserPreferencesImpl.class, FIND_PREFERENCES_BY_DESCRIPTOR_PERSON_PROFILE_CACHE_REGION);
        naturalIdQuery.setNaturalIdParam(StylesheetUserPreferencesImpl_.stylesheetDescriptor, (StylesheetDescriptorImpl)stylesheetDescriptor);
        naturalIdQuery.setNaturalIdParam(StylesheetUserPreferencesImpl_.userId, personId);
        naturalIdQuery.setNaturalIdParam(StylesheetUserPreferencesImpl_.profileId, profileId);
        
        return naturalIdQuery.execute();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#storeStylesheetUserPreferences(org.jasig.portal.layout.om.IStylesheetUserPreferences)
     */
    @Transactional
    @Override
    public void storeStylesheetUserPreferences(IStylesheetUserPreferences stylesheetUserPreferences) {
        this.entityManager.persist(stylesheetUserPreferences);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#deleteStylesheetUserPreferences(org.jasig.portal.layout.om.IStylesheetUserPreferences)
     */
    @Transactional
    @Override
    public void deleteStylesheetUserPreferences(IStylesheetUserPreferences stylesheetUserPreferences) {
        this.entityManager.remove(stylesheetUserPreferences);
    }
}

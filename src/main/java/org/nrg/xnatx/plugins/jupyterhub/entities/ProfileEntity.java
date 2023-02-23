package org.nrg.xnatx.plugins.jupyterhub.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class ProfileEntity extends AbstractHibernateEntity {

    private Profile profile;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", nullable = false)
    @Basic(fetch=FetchType.EAGER)
    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Profile toPojo() {
        this.profile.setId(this.getId());
        return this.profile;
    }

    public static ProfileEntity fromPojo(final Profile pojo) {
        ProfileEntity profileEntity = ProfileEntity.builder()
                .profile(pojo)
                .build();

        if (pojo.getId() != null) {
            profileEntity.setId(pojo.getId());
        }

        return profileEntity;
    }

    public ProfileEntity update(final ProfileEntity update) {
        update.getProfile().setId(this.getId());
        this.setProfile(update.getProfile());
        return this;
    }
}
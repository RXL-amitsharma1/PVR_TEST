package com.rxlogix.odata

import com.rxlogix.OdataService
import grails.util.Holders
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*

class OdataEdmProvider extends CsdlAbstractEdmProvider {

    OdataService getOdataService() {
        return Holders.applicationContext.getBean("odataService")
    }

    @Override
    public List<CsdlSchema> getSchemas() {
        try {
            CsdlSchema schema = new CsdlSchema();
            String dsName = OdataUtils.getDsName()
            schema.setNamespace(OdataSettingsCache.NAMESPACE);
            List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
            OdataSettingsCache.getEntitiesMap(dsName).each { key, value ->
                entityTypes << getEntityType(OdataSettingsCache.getFullQualifiedName(dsName, key))
            }
            schema.setEntityTypes(entityTypes);
            schema.setEntityContainer(getEntityContainer());
            List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
            schemas.add(schema);
            return schemas;
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }


    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        try {
            String entityName = entityTypeName.name
            String dsName = OdataUtils.getDsName()
            return OdataSettingsCache.getEntityFields(dsName, entityName).type
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }


    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        try {
            String dsName = OdataUtils.getDsName()
            if (entitySetName) {
                if (entityContainer.equals(OdataSettingsCache.CONTAINER)) {
                    if (OdataSettingsCache.getEntityMap(dsName, entitySetName)) {
                        CsdlEntitySet entitySet = new CsdlEntitySet()
                        entitySet.setName(entitySetName)
                        entitySet.setType(OdataSettingsCache.getFullQualifiedName(dsName, entitySetName))
                        return entitySet
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        String dsName = OdataUtils.getDsName()
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        OdataSettingsCache.getEntitiesMap(dsName).each { key, value ->
            entitySets.add(getEntitySet(OdataSettingsCache.CONTAINER, key));
        }
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(OdataSettingsCache.CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);
        return entityContainer;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        try {
            if (entityContainerName == null || entityContainerName.equals(OdataSettingsCache.CONTAINER)) {
                CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
                entityContainerInfo.setContainerName(OdataSettingsCache.CONTAINER);
                return entityContainerInfo
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }
}

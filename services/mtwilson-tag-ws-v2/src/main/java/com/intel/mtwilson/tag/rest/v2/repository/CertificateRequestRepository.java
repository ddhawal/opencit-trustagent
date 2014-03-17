/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_CERTIFICATE_REQUEST;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestCollection;
import com.intel.mtwilson.tag.model.CertificateRequestFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateRequestLocator;
import com.intel.mtwilson.tag.model.Selection;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class CertificateRequestRepository extends ServerResource implements SimpleRepository<CertificateRequest, CertificateRequestCollection, CertificateRequestFilterCriteria, CertificateRequestLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public CertificateRequestCollection search(CertificateRequestFilterCriteria criteria) {
        CertificateRequestCollection objCollection = new CertificateRequestCollection();
        DSLContext jooq = null;
        
        try (SelectionDAO selectionDao = TagJdbi.selectionDao()) {
            jooq = TagJdbi.jooq();
            
            SelectQuery sql = jooq.select()
                    .from(MW_TAG_CERTIFICATE_REQUEST) // .join(CERTIFICATE_REQUEST_TAG_VALUE)
                    //.on(CERTIFICATE_REQUEST_TAG_VALUE.CERTIFICATEREQUESTID.equal(CERTIFICATE_REQUEST.ID)))
                    .getQuery();
            if( criteria.id != null ) {
    //            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
                sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.ID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.subjectEqualTo != null  && criteria.subjectEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.SUBJECT.equal(criteria.subjectEqualTo));
            }
            if( criteria.subjectContains != null  && criteria.subjectContains.length() > 0  ) {
                sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.SUBJECT.equal(criteria.subjectContains));
            }
            if( criteria.selectionEqualTo != null  && criteria.selectionEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.SUBJECT.equal(criteria.selectionEqualTo)); // TODO: REMOVE
            }
            if( criteria.selectionContains != null  && criteria.selectionContains.length() > 0  ) {
                sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.SUBJECT.equal(criteria.selectionContains));
            }
            if( criteria.statusEqualTo != null  && criteria.statusEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_CERTIFICATE_REQUEST.STATUS.equal(criteria.statusEqualTo));
            }
            sql.addOrderBy(MW_TAG_CERTIFICATE_REQUEST.ID);
            Result<Record> result = sql.fetch();
            
            log.debug("Got {} records", result.size());
            UUID c = new UUID(); // id of the current certificate request object built, used to detect when it's time to build the next one
            for(Record r : result) {
                if( UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE_REQUEST.ID)) != c ) {
                    c = UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE_REQUEST.ID));
                    CertificateRequest obj = new CertificateRequest();
                    obj.setId(UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE_REQUEST.ID)));
                    obj.setSubject(r.getValue(MW_TAG_CERTIFICATE_REQUEST.SUBJECT));
                    obj.setSelectionId(UUID.valueOf((r.getValue(MW_TAG_CERTIFICATE_REQUEST.SELECTIONID)))); // TODO: remove
                    obj.setStatus(r.getValue(MW_TAG_CERTIFICATE_REQUEST.STATUS));
                    if( r.getValue(MW_TAG_CERTIFICATE_REQUEST.CERTIFICATEID) != null ) { // a Long object, can be null
                        obj.setCertificateId(UUID.valueOf(r.getValue(MW_TAG_CERTIFICATE_REQUEST.CERTIFICATEID))); // a long primitive, cannot set to null;   TODO:  remove
                    }
                    obj.setContent(r.getValue(MW_TAG_CERTIFICATE_REQUEST.CONTENT));
                    obj.setContentType(r.getValue(MW_TAG_CERTIFICATE_REQUEST.CONTENTTYPE));
                    objCollection.getCertificates().add(obj);
                }
            }
            sql.close();
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return objCollection;
    }

    @Override
    public CertificateRequest retrieve(CertificateRequestLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(locator.id);
            if (obj != null) {
                return obj;
            }
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }
        return null;
    }

    @Override
    public void store(CertificateRequest item) {
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(item.getId());
            if (obj == null) {
                log.error("Object with specified id does not exist in the system.");
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No matching certificate request found in the system.");
            }
            
            // Let us check what parameter the user wants to update
            if (item.getAuthorityName() != null && !item.getAuthorityName().isEmpty())
                certRequestDao.updateAuthority(item.getId().toString(), item.getAuthorityName());
            
            if (item.getCertificateId() != null)
                certRequestDao.updateApproved(item.getId().toString(), item.getCertificateId().toString()); // TODO: REMOVE,  approval update should be status only (already done below)
            
            if (item.getStatus() != null && !item.getStatus().isEmpty())
                certRequestDao.updateStatus(item.getId().toString(), item.getStatus());
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }
    }

    
    // similar to ImportConfig command in mtwilson-console
    public PasswordProtection getPasswordProtection() {
            PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            if( !protection.isAvailable() ) {
                protection = PasswordProtectionBuilder.factory().aes(128).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            }
        return protection;
    }
    protected void encrypt(CertificateRequest certificateRequest) throws Exception {
        byte[] plaintext = certificateRequest.getContent();
        
            // NOTE: the base64-encoded value of mtwilson.as.dek is used as the encryption password;  this is different than using the decoded value as the aes-128 key which is currently done for attestation service host connection info
            ByteArrayResource resource = new ByteArrayResource();
            PasswordEncryptedFile passwordEncryptedFile = new PasswordEncryptedFile(resource, My.configuration().getDataEncryptionKeyBase64(), getPasswordProtection());
            passwordEncryptedFile.encrypt(plaintext); // saves it to resource
            
            certificateRequest.setContent(resource.toByteArray()); // encrypted xml file wrapped in rfc822-style message format which indicates the encryption settings
            certificateRequest.setContentType("message/rfc822"); 
        
            // TODO:  PasswordEncryptedFile currently doesn't support passing the plaintext content type, but when it does we need ot pass it so it will be mentioned in the encrypted file's headers, so that we can check it when we decrypt later (to ensure it's application/xml before we try to parse it)
            
    }
    protected void decrypt(CertificateRequest certificateRequest) throws Exception {
            ByteArrayResource resource = new ByteArrayResource(certificateRequest.getContent());
            PasswordEncryptedFile passwordEncryptedFile = new PasswordEncryptedFile(resource, My.configuration().getDataEncryptionKeyBase64());
            byte[] plaintext = passwordEncryptedFile.decrypt(); 
            
            certificateRequest.setContent(plaintext); 
            certificateRequest.setContentType("application/xml"); 
        
            // TODO:  PasswordEncryptedFile currently doesn't provide us the plaintext content type, but when it does we need to check it after we decrypt  and set it on the request object
        
    }
    
    @Override
    public void create(CertificateRequest item) {
        
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao(); 
                SelectionDAO selectionDao = TagJdbi.selectionDao()) {
            
            // Since the user would have specified the selectin name, we need to get the selection id first
            Selection selectionObj = selectionDao.findByName(item.getSelectionName());
            if( selectionObj == null) {
                log.error("Selection {} is not available.", item.getSelectionName());
                setStatus(Status.SERVER_ERROR_INTERNAL);  // cannot make a certificate request without a valid selection
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Specified selection does not exist in the system.");
            }
            
            // Since this is the new certificate request, the certificate id would be null.
            if( item.getStatus() == null || item.getStatus().isEmpty() ) { 
                item.setStatus("New");
            }
            
            encrypt(item);
            
//            certRequestDao.insert(item.getId().toString(), item.getSubject(), selectionObj.getId().toString(), null, null);
            certRequestDao.insert(item);
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate request creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }       
    }

    @Override
    public void delete(CertificateRequestLocator locator) {
        if (locator == null || locator.id == null) { return;}
        try (CertificateRequestDAO certRequestDao = TagJdbi.certificateRequestDao()) {            
            CertificateRequest obj = certRequestDao.findById(locator.id);
            if (obj != null) {
                certRequestDao.deleteById(locator.id);
            }
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }       
    }
    
    @Override
    public void delete(CertificateRequestFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}

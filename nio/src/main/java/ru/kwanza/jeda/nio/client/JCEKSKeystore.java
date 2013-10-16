package ru.kwanza.jeda.nio.client;

import ru.kwanza.jeda.nio.server.http.Const;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static ru.kwanza.jeda.nio.server.http.HttpServer.logger;

/**
 * @author Ivan Baluk
 */
public class JCEKSKeystore {

//    public static final X509Certificate[] ACCEPTED_ISSUERS = new X509Certificate[]{};
//
//    private FileLock keystoreFileLock;
//    private File keystoreFile;
//    private KeyManager[] keyManagers;
//    private PrivateKey serverPrivateKey;
//    private final Set<TrustAnchor> trustedAnchors = new HashSet<TrustAnchor>();
//    private String keyStorePassword;
//    private TrustManager[] trustManagers;
//    private X509Certificate[] serverCertificateChain;
//
//    private class X509TrustManagerImpl implements X509TrustManager {
//        public X509TrustManagerImpl() {
//        }
//
//        public void checkClientTrusted(X509Certificate[] chain, String authType) {
//            try {
//                checkChainTrusted0(chain, trustedAnchors);
//            } catch (CertificateException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//        }
//
//        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            checkChainTrusted0(chain, trustedAnchors);
//        }
//
//        public X509Certificate[] getAcceptedIssuers() {
//            return ACCEPTED_ISSUERS;
//        }
//    }
//
//    private class X509KeyManagerImpl extends X509ExtendedKeyManager {
//        public String[] getClientAliases(String keyType, Principal[] issuers) {
//            throw new UnsupportedOperationException();
//        }
//
//        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
//            throw new UnsupportedOperationException();
//        }
//
//        public String[] getServerAliases(String keyType, Principal[] issuers) {
//            throw new UnsupportedOperationException();
//        }
//
//        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
//            throw new UnsupportedOperationException();
//        }
//
//        public X509Certificate[] getCertificateChain(String alias) {
//            return serverCertificateChain;
//        }
//
//        public PrivateKey getPrivateKey(String alias) {
//            return serverPrivateKey;
//        }
//
//        public String chooseEngineClientAlias(String[] strings, Principal[] principals, SSLEngine sslEngine) {
//            throw new UnsupportedOperationException();
//        }
//
//        public String chooseEngineServerAlias(String s, Principal[] principals, SSLEngine sslEngine) {
//            return Const.SERVER_SLOT_ALIAS;
//        }
//    }
//
//    public JCEKSKeystore(String keystoreFile, String keyStorePassword) {
//        setKeystoreFile(keystoreFile);
//        setKeyStorePassword(keyStorePassword);
//    }
//
//    public void init(String url) {
//        if (!keystoreFile.exists()) {
//            logger.error("HttpClient({}) Keystore file {} doesn't exitst!", url);
//            throw new RuntimeException("File " + keystoreFile.getAbsolutePath() + " doesn't exists!");
//        }
//        loadKeystore();
//        this.trustManagers = trustedAnchors.isEmpty() ? null : new TrustManager[]{new X509TrustManagerImpl()};
//        this.keyManagers = serverPrivateKey == null ? null : new KeyManager[]{new X509KeyManagerImpl()};
//    }
//
//    public void destroy() {
//        try {
//            keystoreFileLock.release();
//        } catch (IOException e) {
//            logger.error("HttpServer(" + server.getName() + ":" + entryPoint.getName()
//                    + ") Can't unlock keystore file " + keystoreFile.getAbsolutePath() + "!", e);
//        }
//    }
//
//    public TrustManager[] getTrustManagers() {
//        return trustManagers;
//    }
//
//    public KeyManager[] getKeyManager() {
//        return keyManagers;
//    }
//
//    public void setKeyStorePassword(String keyStorePassword) {
//        this.keyStorePassword = keyStorePassword;
//    }
//
//    public void setKeystoreFile(String keystoreFile) {
//        this.keystoreFile = new File(keystoreFile);
//    }
//
//    private void checkChainTrusted0(X509Certificate[] chain, Set<TrustAnchor> trustedAnchors)
//            throws CertificateException {
//        try {
//            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
//
//            X509CertSelector selector = new X509CertSelector();
//            selector.setCertificate(chain[0]);
//
//            PKIXBuilderParameters pkix = new PKIXBuilderParameters(trustedAnchors, selector);
//            CertStoreParameters ccsp = new CollectionCertStoreParameters(Arrays.asList(chain));
//            CertStore store = CertStore.getInstance("Collection", ccsp);
//            pkix.addCertStore(store);
//            pkix.setRevocationEnabled(false);
//
//            builder.build(pkix);
//        } catch (CertPathBuilderException e) {
//            for (X509Certificate cert : chain) {
//                cert.checkValidity();
//            }
//            throw new CertificateException(e);
//        } catch (Exception e) {
//            throw new CertificateException(e);
//        }
//    }
//
//    private KeyStore loadFromFile() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
//        RandomAccessFile raf = new RandomAccessFile(keystoreFile, "rw");
//        FileChannel channel = raf.getChannel();
//        keystoreFileLock = channel.lock();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ByteBuffer buffer = ByteBuffer.allocate(64);
//        int bytesRead = channel.read(buffer);
//
//        while (bytesRead != -1) {
//            buffer.flip();
//            while (buffer.hasRemaining()) {
//                baos.write(buffer.get());
//            }
//
//            buffer.clear();
//            bytesRead = channel.read(buffer);
//        }
//
//        KeyStore ks = KeyStore.getInstance("JCEKS");
//        char[] password = keyStorePassword.toCharArray();
//        ks.load(new ByteArrayInputStream(baos.toByteArray()), password);
//        return ks;
//    }
//
//    private void loadKeystore() {
//        try {
//            KeyStore ks = loadFromFile();
//
//            Enumeration<String> aliases = ks.aliases();
//            while (aliases.hasMoreElements()) {
//                String alias = aliases.nextElement();
//                if (ks.isCertificateEntry(alias)) {
//                    Certificate[] chain = ks.getCertificateChain(alias);
//                    X509Certificate[] certificateChain = new X509Certificate[chain.length];
//                    System.arraycopy(chain, 0, certificateChain, 0, chain.length);
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("HttpServer({}:{}) Load alias '{}' from file {}, certificate chain: {}",
//                                new Object[]{server.getName(), entryPoint.getName(), alias,
//                                        keystoreFile.getAbsolutePath(), certificateChain});
//                    }
//                    if (Const.SERVER_SLOT_ALIAS.equals(alias)) {
//                        serverPrivateKey = (PrivateKey) ks.getKey(alias, keyStorePassword.toCharArray());
//                        serverCertificateChain = certificateChain;
//                    } else {
//                        for (X509Certificate c : certificateChain) {
//                            trustedAnchors.add(new TrustAnchor(c, null));
//                        }
//                    }
//                } else {
//                    logger.warn("Not found a certificate with alias = " + alias);
//                }
//            }
//        } catch (Exception e) {
////            logger.error("HttpServer({}:{}) Error initializing keystore from file {}",
////                    new Object[]{server.getName(), entryPoint.getName(), keystoreFile.getAbsolutePath()});
//            throw new RuntimeException("Error while initializing keystore from file  " + keystoreFile.getAbsolutePath(), e);
//        }
//    }
}

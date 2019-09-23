import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.security.*;
/**
 * TxHandler Tester.
 *
 * @author <Authors name>
 * @since <pre>9.21.2019</pre>
 * @version 1.0
 */

public class TxHandlerTest extends TestCase {
    private Transaction initTx;
    private TxHandler txHandler;
    private KeyPair Akp;
    private KeyPair Bkp;
    private KeyPair Ckp;
    private KeyPair Dkp;


   @Before
   protected void setUp() throws Exception {
        super.setUp();
        Keypair();
        initCoins();
    }

    private void initCoins() {
        initTx = new Transaction();
        initTx.addOutput(100, Akp.getPublic());
        initTx.finalize();

        UTXOPool pool = new UTXOPool();
        UTXO utxo = new UTXO(initTx.getHash(), 0);
        pool.addUTXO(utxo, initTx.getOutput(0));

        txHandler = new TxHandler(pool);
    }

    private void Keypair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        Akp = kpg.generateKeyPair();
        Bkp = kpg.generateKeyPair();
        Ckp = kpg.generateKeyPair();
        Dkp = kpg.generateKeyPair();
    }

    @Test
    public void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        //B to B 100
        Transaction tx1 = new Transaction();
        tx1.addInput(initTx.getHash(), 0);
        tx1.addOutput(100, Bkp.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(Bkp.getPrivate());
        sig.update(tx1.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        tx1.addSignature(sig1, 0);
        tx1.finalize();
        assertFalse(txHandler.isValidTx(tx1));

        //A to B 100
        Transaction tx2 = new Transaction();
        tx2.addInput(initTx.getHash(), 0);
        tx2.addOutput(100, Bkp.getPublic());
        sig.initSign(Akp.getPrivate());
        sig.update(tx2.getRawDataToSign(0));
        byte[] sig2 = sig.sign();
        tx2.addSignature(sig2, 0);
        tx2.finalize();
        assertTrue(txHandler.isValidTx(tx2));

        //A to B 40, to C 60
        Transaction tx3 = new Transaction();
        tx3.addInput(initTx.getHash(), 0);
        tx3.addOutput(40, Bkp.getPublic());
        tx3.addOutput(60, Ckp.getPublic());
        sig.initSign(Akp.getPrivate());
        sig.update(tx3.getRawDataToSign(0));
        byte[] sig3 = sig.sign();
        tx3.addSignature(sig3, 0);
        tx3.finalize();
        assertTrue(txHandler.isValidTx(tx3));
    }

    public void test2()
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
       //A to B 40, to C 70 ,not valid
       Transaction tx = new Transaction();
        tx.addInput(initTx.getHash(), 0);
        tx.addOutput(40, Bkp.getPublic());
        tx.addOutput(70, Ckp.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(Akp.getPrivate());
        sig.update(tx.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        tx.addSignature(sig1, 0);
        tx.finalize();
        assertFalse(txHandler.isValidTx(tx));

        //A to B 40, to C -70, not valid
        Transaction tx1 = new Transaction();
        tx1.addInput(initTx.getHash(), 0);
        tx1.addOutput(40, Bkp.getPublic());
        tx1.addOutput(-70, Ckp.getPublic());
        sig.initSign(Akp.getPrivate());
        sig.update(tx1.getRawDataToSign(0));
        byte[] sig2 = sig.sign();
        tx1.addSignature(sig2, 0);
        tx1.finalize();
        assertFalse(txHandler.isValidTx(tx1));
    }

    public void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        // A to B 100
        Transaction tx1 = new Transaction();
        tx1.addInput(initTx.getHash(), 0);
        tx1.addOutput(100, Bkp.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(Akp.getPrivate());
        sig.update(tx1.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        tx1.addSignature(sig1, 0);
        tx1.finalize();

        assertTrue(txHandler.isValidTx(tx1));
        Transaction[] tx11 = txHandler.handleTxs(new Transaction[] { tx1 });

        // B to C 50, to D 50
        Transaction tx2 = new Transaction();
        tx2.addInput(tx1.getHash(), 0);
        tx2.addOutput(50, Ckp.getPublic());
        tx2.addOutput(50, Dkp.getPublic());
        sig.initSign(Bkp.getPrivate());
        sig.update(tx2.getRawDataToSign(0));
        byte[] sig2 = sig.sign();
        tx2.addSignature(sig2, 0);
        tx2.finalize();

        assertTrue(txHandler.isValidTx(tx2));
        Transaction[] tx22 = txHandler.handleTxs(new Transaction[] { tx2 });
    }

    public void test4()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        // A to B 100
        Transaction tx1 = new Transaction();
        tx1.addInput(initTx.getHash(), 0);
        tx1.addOutput(100, Bkp.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(Akp.getPrivate());
        sig.update(tx1.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        tx1.addSignature(sig1, 0);
        tx1.finalize();

        // B to C 50, to D 50
        Transaction tx2 = new Transaction();
        tx2.addInput(tx1.getHash(), 0);
        tx2.addOutput(50, Ckp.getPublic());
        tx2.addOutput(50, Dkp.getPublic());
        sig.initSign(Bkp.getPrivate());
        sig.update(tx2.getRawDataToSign(0));
        byte[] sig2 = sig.sign();
        tx2.addSignature(sig2, 0);
        tx2.finalize();

        // C to D 40
        Transaction tx3 = new Transaction();
        tx3.addInput(tx2.getHash(), 0);
        tx3.addOutput(40, Dkp.getPublic());
        sig.initSign(Ckp.getPrivate());
        sig.update(tx3.getRawDataToSign(0));
        byte[] sig3 = sig.sign();
        tx3.addSignature(sig3, 0);
        tx3.finalize();

        Transaction[] tx = txHandler.handleTxs(new Transaction[] { tx1, tx2, tx3 });
        assertEquals(tx.length, 3);
    }

    public void test5()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        // A to B 100
        Transaction tx1 = new Transaction();
        tx1.addInput(initTx.getHash(), 0);
        tx1.addOutput(100, Bkp.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(Akp.getPrivate());
        sig.update(tx1.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        tx1.addSignature(sig1, 0);
        tx1.finalize();

        assertTrue(txHandler.isValidTx(tx1));
        Transaction[] tx = txHandler.handleTxs(new Transaction[] { tx1 });

        // B to C 100
        Transaction tx2 = new Transaction();
        tx2.addInput(tx1.getHash(), 0);
        tx2.addOutput(100, Ckp.getPublic());
        sig.initSign(Bkp.getPrivate());
        sig.update(tx2.getRawDataToSign(0));
        byte[] sig2 = sig.sign();
        tx2.addSignature(sig2, 0);
        tx2.finalize();
        assertTrue(txHandler.isValidTx(tx2));
        tx = txHandler.handleTxs(new Transaction[] { tx2 });

        // B to D 100
        Transaction tx3 = new Transaction();
        tx3.addInput(tx1.getHash(), 0);
        tx3.addOutput(100, Ckp.getPublic());
        sig.initSign(Bkp.getPrivate());
        sig.update(tx3.getRawDataToSign(0));
        byte[] sig3 = sig.sign();
        tx3.addSignature(sig3, 0);
        tx3.finalize();
        assertFalse(txHandler.isValidTx(tx3));
        tx = txHandler.handleTxs(new Transaction[] { tx3 });
    }
}

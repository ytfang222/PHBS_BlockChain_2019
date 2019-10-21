import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import junit.framework.TestCase;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.equalTo;

/** 
* BlockChain Tester. 
* 
* @author <Authors name> 
* @since <pre>10月 16, 2019</pre>
* @version 1.0 
*/ 
public class BlockChainTest extends TestCase {
    private Block genesis;
    private BlockChain bc;
    private BlockHandler bh;
    private KeyPair pkA;
    private KeyPair pkB;
    private KeyPair pkC;
    private KeyPair pkD;
    private KeyPair pkE;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        pkA = kpg.generateKeyPair();
        pkB = kpg.generateKeyPair();
        pkC = kpg.generateKeyPair();
        pkD = kpg.generateKeyPair();
        pkE = kpg.generateKeyPair();
    }

    public static class Tx extends Transaction {
        public void signTx(PrivateKey sk, int input) throws SignatureException {
            Signature sig = null;
            try {
                sig = Signature.getInstance("SHA256withRSA");
                sig.initSign(sk);
                sig.update(this.getRawDataToSign(input));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            this.addSignature(sig.sign(), input);
            this.finalize();
        }
    }

    @Test
    //Test01: Process a genesis block
    public void test01()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);
        assertTrue(bc.getMaxHeightUTXOPool().contains(new UTXO(genesisblock.getCoinbase().getHash(),0)));
    }

    @Test
    //Test02: Process a block with valid transactions and process a block with invalid tx.
    public void test02()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);
        Block block1 = new Block(genesisblock.getHash(), pkB.getPublic());

        // tx1: A pays 25 coins to B True
        Tx tx1 = new Tx();
        tx1.addInput(genesisblock.getCoinbase().getHash(), 0);
        tx1.addOutput(4, pkB.getPublic());
        tx1.addOutput(5, pkB.getPublic());
        tx1.addOutput(6, pkB.getPublic());
        tx1.addOutput(10, pkC.getPublic());
        tx1.signTx(pkA.getPrivate(), 0);
        block1.addTransaction(tx1);
        block1.finalize();
        bh.processBlock(block1);
        assertTrue(bc.getMaxHeightUTXOPool().contains(new UTXO(block1.getCoinbase().getHash(),0)));
        assertTrue(bc.getMaxHeightBlock().getHash()==block1.getHash());

        Block block2 = new Block(block1.getHash(), pkB.getPublic());
        // tx2: A pays 40 coins to B  False
        Tx tx2 = new Tx();
        tx2.addInput(block1.getHash(), 0);
        tx2.addOutput(40, pkB.getPublic());
        tx2.signTx(pkA.getPrivate(), 0);
        block2.addTransaction(tx2);
        block2.finalize();
        bh.processBlock(block2);

        assertTrue(bh.processBlock(block1));
        assertFalse(bh.processBlock(block2));
        assertTrue(bc.getMaxHeightBlock() == block1);
        assertTrue(bc.getMaxHeightUTXOPool().getAllUTXO().size()==5);
    }

    @Test
    //Test03: Process a block with an invalid prevBlockHash
    public void test03()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);

        byte[] hash = genesisblock.getHash();
        byte[] hashCopy = Arrays.copyOf(hash, hash.length);
        hashCopy[0]++;
        Block block1 = new Block(hashCopy, pkB.getPublic());
        block1.finalize();
        bh.processBlock(block1);
        assertFalse(bh.processBlock(block1));
    }

    @Test
    public void test04()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        //Test04:Create a block after a valid transaction has been processed.
        Block genesisBlock = new Block(null, pkA.getPublic());
        genesisBlock.finalize();
        BlockChain bc = new BlockChain(genesisBlock);
        BlockHandler bh = new BlockHandler(bc);
        Block block = new Block(genesisBlock.getHash(), pkB.getPublic());

        Tx tx1 = new Tx();
        tx1.addInput(genesisBlock.getCoinbase().getHash(), 0);
        tx1.addOutput(25, pkB.getPublic());
        tx1.signTx(pkA.getPrivate(), 0);
        tx1.finalize();
        block.addTransaction(tx1);
        block.finalize();

        Tx tx2 = new Tx();
        tx2.addInput(genesisBlock.getCoinbase().getHash(), 0);
        tx2.addOutput(25, pkB.getPublic());
        tx2.signTx(pkA.getPrivate(), 0);
        tx2.finalize();
        bh.processTx(tx2);
        Block createdBlock = bh.createBlock(pkB.getPublic());

        assertTrue(bc.getMaxHeightBlock() == createdBlock);
        assertTrue(bc.getMaxHeightUTXOPool().getAllUTXO().size() == 2);

    }

    @Test
    //Test05:Process a linear chain of blocks and return the maxHeightBlock
    public void test05()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);
        Block block1 = new Block(genesisblock.getHash(), pkB.getPublic());
        block1.finalize();
        System.out.println("Block1 is ok: " + bh.processBlock(block1));

        Block blockB2 = new Block(block1.getHash(), pkB.getPublic());
        blockB2.finalize();
        bh.processBlock(blockB2);
        System.out.println("BlockB2 is ok: " + bh.processBlock(blockB2));

        Block block3 = new Block(block1.getHash(), pkC.getPublic());
        block3.finalize();
        System.out.println("Block3 is ok: " + bh.processBlock(block3));

        Block block4 = new Block(block3.getHash(), pkD.getPublic());
        block4.finalize();
        System.out.println("Block4 is ok: " + bh.processBlock(block4));

        Block block5 = new Block(block4.getHash(), pkD.getPublic());
        block5.finalize();
        System.out.println("Block5 is ok: " + bh.processBlock(block5));

        Block block6 = new Block(block5.getHash(), pkE.getPublic());
        block6.finalize();
        System.out.println("Block6 is ok: " + bh.processBlock(block6));

        Block blockB5 = new Block(block5.getHash(), pkD.getPublic());
        blockB5.finalize();
        System.out.println("BlockB5 is ok: " + bh.processBlock(blockB5));

        Block blockB55 = new Block(block5.getHash(), pkD.getPublic());
        blockB55.finalize();
        bc.getMaxHeightBlock();
        System.out.println("BlockB55 is ok: " + bh.processBlock(blockB55));
        assertTrue(bc.getMaxHeightBlock().getHash() == block6.getHash());
    }

    @Test
    public void test06()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        //Test06:Test transactionpool:Add a transaction into the txpool,
        // after tx write into the block remove it from the txpool.
        Block genesisBlock = new Block(null, pkA.getPublic());
        genesisBlock.finalize();
        BlockChain bc = new BlockChain(genesisBlock);
        BlockHandler bh = new BlockHandler(bc);
        Tx tx1 = new Tx();
        tx1.addInput(genesisBlock.getCoinbase().getHash(), 0);
        tx1.addOutput(25, pkB.getPublic());
        tx1.signTx(pkA.getPrivate(), 0);
        tx1.finalize();
        bc.addTransaction(tx1);
        assertTrue(bc.getTransactionPool().getTransactions().size() == 1);

        Block block = new Block(genesisBlock.getHash(), pkB.getPublic());
        block.addTransaction(tx1);
        block.finalize();
        bh.processBlock(block);
        assertTrue(bc.addBlock(block));
        bc.getTransactionPool().removeTransaction(tx1.getHash());
        assertTrue(bc.getTransactionPool().getTransactions().isEmpty());
    }

    //The following part contains multi branch in blockchain.
    @Test
    //Test 07: Process multiple blocks branch on top of the genesis block, and return the oldest block.
    public void test07()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);

        Block block1 = new Block(genesisblock.getHash(), pkB.getPublic());
        block1.finalize();
        System.out.println("Block1 is ok: " + bh.processBlock(block1));
        Block block2 = new Block(genesisblock.getHash(), pkB.getPublic());
        block2.finalize();
        System.out.println("Block2 is ok: " + bh.processBlock(block2));
        Block block3 = new Block(genesisblock.getHash(), pkB.getPublic());
        block3.finalize();
        System.out.println("Block3 is ok: " + bh.processBlock(block3));

        assertTrue(bc.getMaxHeightBlock().getHash() == block1.getHash());
    }

    @Test
    //Test08：Process a block containing a transaction that is already used by a transaction in its parent
    //result:False
    public void test08()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);

        Block block1 = new Block(genesisblock.getHash(), pkB.getPublic());
        Tx tx1 = new Tx();
        tx1.addInput(genesisblock.getCoinbase().getHash(), 0);
        tx1.addOutput(25, pkB.getPublic());
        tx1.signTx(pkA.getPrivate(), 0);
        block1.addTransaction(tx1);
        block1.finalize();
        bh.processBlock(block1);

        Block prevBlock = block1;
        block1 = new Block(prevBlock.getHash(), pkB.getPublic());
        //Tx tx1 = new Tx();
        tx1.addInput(genesisblock.getCoinbase().getHash(), 0);
        tx1.addOutput(24, pkB.getPublic());
        tx1.signTx(pkB.getPrivate(), 0);
        block1.addTransaction(tx1);
        block1.finalize();
        assertFalse(bh.processBlock(block1));
    }

    @Test
    //Test09:Process a linear chain of blocks of length CUT_OFF_AGE and then a block on top of the genesis block
    public void test09()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);
        Block block;
        Block prevBlock = genesisblock;

        for (int i = 0; i < BlockChain.CUT_OFF_AGE; i++) {
            block = new Block(prevBlock.getHash(), pkA.getPublic());
            block.finalize();
            prevBlock = block;
            bh.processBlock(block);
            System.out.println("Block is ok: " + bh.processBlock(block));
        }
        block = new Block(genesisblock.getHash(), pkA.getPublic());
        block.finalize();
        assertTrue(bh.processBlock(block));
    }

    @Test
    //Test10: Process a linear chain of blocks of length CUT_OFF_AGE + 1
    //and then a block on top of the genesis block
    //the result is false. compare this situation to test08.
    public void test10()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Block genesisblock = new Block(null, pkA.getPublic());
        genesisblock.finalize();
        BlockChain bc = new BlockChain(genesisblock);
        BlockHandler bh = new BlockHandler(bc);
        Block block;
        Block prevBlock = genesisblock;

        for (int i = 0; i < BlockChain.CUT_OFF_AGE + 1; i++) {
            block = new Block(prevBlock.getHash(), pkA.getPublic());
            block.finalize();
            prevBlock = block;
            bh.processBlock(block);
        }
        block = new Block(genesisblock.getHash(), pkA.getPublic());
        block.finalize();
        assertFalse(bh.processBlock(block));
    }
}
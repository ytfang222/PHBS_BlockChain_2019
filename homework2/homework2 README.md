### homework2

------

Name:Fang Yuting 

Student ID: 1901212576

This homework aims at receiving incoming transactions and blocks and maintain an updated block chain. We verify the transactions and blocks and add the block to the maintain blockchain. 

For the assignment, we're given a data structure to keep record of the block chain:

```
private class BlockNode{
public Block block;
public BlockNode parentblock;
public ArrayList<BlockNode> children;
}
```

This is a recursive data type that represent a tree and we can tranverse it up and down.

We can query the block passed as the arguement to addBlock() to find the previous block hash, that is, the hash of the block in the chain that this block is building on.

------

The summary of the homework solution will be two parts. In first part, I show how the block chain works and the description of the blockchain.java. In second part, I design several test cases together with its description and purpose.

Before this, we have to know the function of each javaclass. 

- In Block.java file, the Block generates by prevHash and pk address, transactions are stored as lists in the block.The TxHandler(UTXOPool) class in hw1 deal with the all Transaction. 

- In BlockHandler.java file, we know three methods to operate the block. As a blockchain user, we should test this file in BlockchainTest.java. However, we can directly test the blockchain.java. These two things remain the same outcome.

processBlock：add a newly received block

```JAVA
public boolean processBlock(Block block) {
  if (block == null) return false;
    return blockChain.addBlock(block);}
```

createBlock：create a new block over the maxheight. After we receive a transaction, we can use this function to add the transaction to a new block.

processTx: add a newly received transaction .

```JAVA
public void processTx(Transaction tx) {blockChain.addTransaction(tx);}
```

- BlockChain.class

Every block is contained in a BlockNode. The parentNode.block.getHash() is equal to prevBlockHash.

In hint, we can also find some useful information.

1.A genesis block won't be mined.

2.If two blocks are in the same height, we will add the new block into the oldest block.(Time is early.)

3.A coinbase transaction of a block is available to be spent in the next block mined on top of it.

4.There are only one global transaction pool and we can add or remove transaction after a new block is created.

5.In order to check the validity of a new block, we just need to check if the transactions are valid. We don't need to do proof-of-work checks.

------



#### Solution Part1:Blockchain.java：

1、public BlockChain(Block genesisBlock) 

I design three methond before creating a  genesis block. 

- I create a function called updateMaxHeightNode(). It will return the hightest BlockNode and if two block are in the same height, it will return the oldest time one which satisfies the hint2.

- The BlockNode depends on it parentNode so I create a function getParentNode(byte[] blockHash) .

- class BlockNode contains height、utxopool、transactionpool and their method. Each block belongs to one node.

Then I create Block genesisBlock. In this function, I new utxopool to add UTXO into the genesisBlock and I new txpool to add valid transaction into the block. After these, it will return BlockNode+1 and update the maxHeightNode and add blockNode into blockChain.

2、public Block getMaxHeightBlock() 

It returns this.maxHeightNode.block.

3、public UTXOPool getMaxHeightUTXOPool() 

It returns this.maxHeightNode.utxoPool.

4、public TransactionPool getTransactionPool()

It returns this.txPool.

5、public boolean addBlock(Block block) 

There are several rules which need to check whether the block is valid or not  so that we can add newBlock. 

- prevBlockHash is not null. In other words, the block is not the genesisBlock.
- parentNode is not null and parentNode is an appropriate node.
- blockHeight >= maxHeightNode.height- CUT_OFF_AGE. What would not be Ok in this example is if addBlock() were called with a block that buildsHeight >= maxHeightNode.height- CUT_OFF_AGE.
- All transactions are valid. This is the same rule as we check in the homework1.

6、public void addTransaction(Transaction tx) 

It returns txPool.addTransaction(tx)

------

The way to add block like the following.

```Java
Block genesisblock = new Block(null, pkA.getPublic());
genesisblock.finalize();
BlockChain bc = new BlockChain(genesisblock);
BlockHandler bh = new BlockHandler(bc);
Block block1 = new Block(genesisblock.getHash(), pkB.getPublic());
```

The way to add transaction like the following.

```Java
Tx tx1 = new Tx();
tx1.addInput(genesisblock.getCoinbase().getHash(), 0);
tx1.addOutput(25, pkB.getPublic());
tx1.signTx(pkA.getPrivate(), 0);
block1.addTransaction(tx1);
bh.processBlock(block1)
```

#### Solution Part2:Blockchaintest.java：

I design 10 Junit tests to test the function in the BlockChain.Java. BlockHandler.java uses all the function which are written in the BlockChain.java. We just need to test three main function processBlock、createdBlock、processTx in BlockHandler.java so that we can test all the function defined in the BlockChain.java. The following Junit test use many possible cases happening in this Blockchain machanism to design tests.

- Test01: test a block with no transactions. This test aim at testing processBlock.


- Test02: test  block1 with valid transaction and add a new block2 with inValid transaction which can not be added.This test aims at test addTransaction() in BlockChain.java which is the same funcion of processTX in BlockHandler.java.

- Test03: test a block with an invalid prevBlockHash.

- Test04: create a block after a valid transaction has been processed. This test aim at test CreateBlock.

- Test05: test a linear chain of blocks and it has two branchs. The path is like this. We can see that blockB2,blockB5,blockB55 is the side branch. If we receive new block, it will add to the longest branch.

![images](https://github.com/ytfang222/PHBS_BlockChain_2019/raw/master/homework2/block.png)
- Test06: After a transaction happening, we add a transaction into the txpool. After the transaction write into the block, test the function of remove it from the txpool.


- Test07:test a linear chain of blocks add on the top of the genesis block, and then return the oldest block as the hint2.

- Test08: test a block containing a transaction that is already used by a transaction in its parent, the result is False.

- Test09:test a linear chain of blocks of length CUT_OFF_AGE and then a block on top of the genesis block. The result is true.


- Test10: test a linear chain of blocks of length CUT_OFF_AGE + 1 and then a block on top of the genesis block. The result is false. This means that the length of long branch minus short branch > CUT_OFF_AGE, then we assume the short branch will never add new block.

#### Solution Part3:test results

![images](https://github.com/ytfang222/PHBS_BlockChain_2019/raw/master/homework2/testresult.png)








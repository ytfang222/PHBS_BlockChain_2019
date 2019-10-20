import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TxHandler {
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
        // this表示当前类，this.utxoPool是当前类utxoPool的属性
        // new UTXOPool实例化一个UTXOPool的对象，UTXOPool是UTXO接口的实现类
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */

    public boolean isValidTx(Transaction tx) {
        // For Rule5
        double outputSum = 0;
        double inputSum = 0;

        for (Transaction.Input input : tx.getInputs()) {
            // Rule1
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (!utxoPool.contains(utxo)) return false;

            // Rule2
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            if (output == null) return false;
            inputSum += output.value;
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(tx.getInputs().indexOf(input)), input.signature)) {
                return false;
            }

            // Rule3
            UTXOPool claimedUtxo = new UTXOPool();
            if (claimedUtxo.contains(utxo)) return false;
            claimedUtxo.addUTXO(utxo, output);
        }

        for (Transaction.Output output : tx.getOutputs()) {
            // Rule4
            if (output.value < 0) return false;
            outputSum += output.value;
        }

        // Rule5
        if (inputSum < outputSum) return false;
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Set<Transaction> transactions = new HashSet<Transaction>();

        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                transactions.add(tx);
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output output = tx.getOutput(i);
                    utxoPool.addUTXO(new UTXO(tx.getHash(), i), output);
                }
            }

        }
        Transaction[] validArr = new Transaction[transactions.size()];
        return transactions.toArray(validArr);

    }
}



import java.util.HashMap;
import java.security.PublicKey;
import java.util.ArrayList;

public class TxHandler {

    private UTXOPool ledger;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        ledger = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        // I'm verifying if the {@code tx} is valid transaction from ledger

        double outputSum = 0;
        double inputSum = 0;

        for(Transaction.Output output: tx.getOutputs())
        {
            //(4) all of {@code tx}s output values are non-negative, and
            if(output.value<0)
                return false;
            outputSum+=output.value;
        }

        HashMap<UTXO,Boolean> utxosOfTransaction = new HashMap<UTXO,Boolean>();

        int index=0;
        for(Transaction.Input input: tx.getInputs())
        {
            //input doesn't have coin value, I need to find transaction from ledger and from that transaction get the value
            UTXO inputUTXO = new UTXO(input.prevTxHash,input.outputIndex);

            //(3) no UTXO is claimed multiple times by {@code tx}
            if (utxosOfTransaction.get(inputUTXO)==null)
                utxosOfTransaction.put(inputUTXO,true);
            else
                return false;

            Transaction.Output output = ledger.getTxOutput(inputUTXO);
            
            //(1) all outputs claimed by {@code tx} are in the current UTXO pool
            if(output == null)
                return false;

            //(2) the signatures on each input of {@code tx} are valid
            byte[] message = tx.getRawDataToSign(index);
            PublicKey pubKey = output.address; // it's public key of previous owner of coin

            boolean isValidSignature = Crypto.verifySignature(pubKey, message,input.signature);
            if (!isValidSignature)
                return false; 

            inputSum+=output.value;

            index++;
        }

        System.out.println(inputSum);
        System.out.println(outputSum);

        //(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        if (outputSum>inputSum)
            return false;

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        
        ArrayList<Transaction> verifiedTransactions = new ArrayList<Transaction>();

        // verify every single transaction validity
        for(Transaction transaction: possibleTxs)
        {
            if(this.isValidTx(transaction))
            {
                verifiedTransactions.add(transaction);

                //remove consumed coins from local pool
                ArrayList<Transaction.Input> inputs = transaction.getInputs();
                for(int i=0; i<inputs.size();i++)
                {
                    Transaction.Input input = inputs.get(i);
                    UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
                    this.ledger.removeUTXO(utxo);
                }

                //add recently created coins 
                ArrayList<Transaction.Output> outputs = transaction.getOutputs();
                for(int i=0;i<outputs.size();i++)
                {
                    Transaction.Output output = outputs.get(i);
                    UTXO utxo = new UTXO(transaction.getHash(),i);
                    ledger.addUTXO(utxo, output);
                }
            }
                    
            
        } 

        //TODO update local UTXO pool => remove consumed coins and add new created coins

        //TODO validate mutually - 

        Transaction[] transactions = new Transaction[verifiedTransactions.size()];
        return verifiedTransactions.toArray(transactions);
    }

}

package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.NegotiationContract
import com.template.states.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

class ProposalFlow  {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(
            val isBuyer: Boolean,
            val amount: Int,
            val counterParty: Party

    ): FlowLogic<UniqueIdentifier>(){
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call(): UniqueIdentifier {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

            // Create output
            val (buyer, seller) = when {
                isBuyer -> ourIdentity to counterParty
                else -> counterParty to ourIdentity
            }
            val output = ProposalState(amount, buyer, seller, ourIdentity, counterParty);


            // Create the command
            val commandType = NegotiationContract.Commands.Propose()
            val requiredSigners = listOf(ourIdentity.owningKey, counterParty.owningKey)
            val command = Command(commandType, requiredSigners);

            //Build the transaction.
            val notary = serviceHub.networkMapCache.notaryIdentities.first();
            val txBuilder = TransactionBuilder(notary);

            txBuilder.addOutputState(output, NegotiationContract.ID)
            txBuilder.addCommand(command);

            // Signing the transaction ourselves.
            val partySign = serviceHub.signInitialTransaction(txBuilder);

            //Gather the counterparty's signature
            val counterPartySession = initiateFlow(counterParty);
            val fullySignTx = subFlow(CollectSignaturesFlow(partySign, listOf(counterPartySession)));

            //Finalizing the transaction
            val finalizedTx = subFlow(FinalityFlow(fullySignTx, listOf(counterPartySession)))

            return finalizedTx.tx.outputsOfType<ProposalState>().single().linearId;

        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterPartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val signedTransactionFlow = object: SignTransactionFlow(counterPartySession){
                override fun checkTransaction(stx: SignedTransaction) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }

            val txId = subFlow(signedTransactionFlow).id
            subFlow(ReceiveFinalityFlow(counterPartySession, txId))
        }

    }
}
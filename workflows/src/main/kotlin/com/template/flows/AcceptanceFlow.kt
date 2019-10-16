package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.NegotiationContract
import com.template.states.ProposalState
import com.template.states.TradeState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

class AcceptanceFlow{

    @InitiatingFlow
    @StartableByRPC
    class Initiator(val proposalId: UniqueIdentifier) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()


        override fun call() {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

            // Retrieve the input from the vault
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(proposalId))
            println("Printing inputCriteria : "+inputCriteria.toString());

            println("Printing list of StateAndRef : "+serviceHub.vaultService.queryBy<ProposalState>(inputCriteria).states.toString());

            val inputStateAndRef = serviceHub.vaultService.queryBy<ProposalState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data

            // Creating the output.
            val output = TradeState(input.amount, input.buyer, input.seller, input.linearId);

            // Creating the command.
            val requiredSigners = listOf(input.proposer.owningKey, input.proposee.owningKey);
            val command = Command(NegotiationContract.Commands.Accept(), requiredSigners);

            // Build the transaction
            val notary = inputStateAndRef.state.notary;
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addInputState(inputStateAndRef)
            txBuilder.addOutputState(output, NegotiationContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves

            var partTx = serviceHub.signInitialTransaction(txBuilder)


            //Gather the counter party's signature
            val counterParty = if (ourIdentity == input.proposer) input.proposee else input.proposer
            val counterPartySession = initiateFlow(counterParty)
            val fullySignTx = subFlow(CollectSignaturesFlow(partTx, listOf(counterPartySession)))

            subFlow(FinalityFlow(fullySignTx, listOf(counterPartySession)))

        }

    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterPartySession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val signTransactionFlow = object : SignTransactionFlow(counterPartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    val ledgerTx = stx.toLedgerTransaction(serviceHub, false);
                    val proposee = ledgerTx.inputsOfType<ProposalState>().single().proposee

                    if (proposee != counterPartySession.counterparty) {
                        throw FlowException("Only the proposee can accept a proposal. ")
                    }
                }
            }
            val txId = subFlow(signTransactionFlow).id
            subFlow(ReceiveFinalityFlow(counterPartySession, txId));
        }
    }
}
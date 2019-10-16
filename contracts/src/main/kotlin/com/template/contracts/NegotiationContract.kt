package com.template.contracts

import com.template.states.ProposalState
import com.template.states.TradeState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction


class NegotiationContract : Contract {

    companion object {
        const val ID = "com.template.contracts.NegotiationContract";
    }

    override fun verify(tx: LedgerTransaction) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        // get commands
        val cmd = tx.commands.requireSingleCommand<Commands>()

        when (cmd.value){

            /**
             *  1) There no inputs.
             *  2) There is exactly one output.
             *  3) The single output is of type ProposalState.
             *  4) There is exactly one command.
             *  5) There is no timestamp.
             *  6) The buyer and seller are  proposer and proposee
             *  7) The proposer is the required signer.
             *  8) The proposee is a required signer.
             */
            is Commands.Propose -> requireThat {
                "There are no inputs" using (tx.inputStates.isEmpty())
                "There is exactly one output " using (tx.outputStates.size == 1)
                "The single output is of type ProposalState" using(tx.outputsOfType<ProposalState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1);
                "There is no timestamp" using (tx.timeWindow == null)

                val outputList = tx.outputsOfType<ProposalState>()
                val output = tx.outputsOfType<ProposalState>().single()

                println("Printing proposal list : "+outputList.toString());
                println("Printing proposal state : "+output.toString());

                "The buyer and seller are the proposer and proposee" using (setOf(output.proposer, output.proposee) == setOf(output.buyer, output.seller))
                "The proposer is a required signer" using (cmd.signers.contains(output.proposer.owningKey))
                "The proposee is required signer" using (cmd.signers.contains(output.proposee.owningKey))
            }

            is Commands.Accept -> requireThat {
                "There is exactly one input " using (tx.inputStates.size == 1)
                "The single input is of type ProposalState" using (tx.inputsOfType<ProposalState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type TradeState" using (tx.outputsOfType<TradeState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                // check input and output values
                val input = tx.inputsOfType<ProposalState>().single()
                val output = tx.outputsOfType<TradeState>().single()

                "The amount is unmodified in the output" using (input.amount == output.amount)
                "The buyer is unmodified" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)

                //Check required signers
                "The proposer is a required signer" using (cmd.signers.contains(input.proposer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(input.proposee.owningKey))
            }

            is Commands.Modify -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type ProposalState" using (tx.inputsOfType<ProposalState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type ProposalState" using (tx.outputsOfType<ProposalState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<ProposalState>().single()
                val input = tx.inputsOfType<ProposalState>().single()

                "The amount is modified in the output" using (output.amount != input.amount)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)

                "The proposer is a required signer" using (cmd.signers.contains(output.proposer.owningKey))
                "The proposee is a required signer" using (cmd.signers.contains(output.proposee.owningKey))

            }
        }


    }


    interface Commands : CommandData {
        class Propose : Commands
        class Accept : Commands
        class Modify : Commands
    }
}
package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.states.ProposalState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.ProgressTracker

class ModificationFlow{



    class Initiator(val proposalId: UniqueIdentifier, val newAmount: Int) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            //Retrieving the input from the vault.
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(proposalId))
            val inputStateAndRef = serviceHub.vaultService.queryBy<ProposalState>(inputCriteria).states.single()
            val input = inputStateAndRef

        }



    }
}
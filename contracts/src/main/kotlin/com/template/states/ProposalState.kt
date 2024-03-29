package com.template.states

import com.template.contracts.NegotiationContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(NegotiationContract::class)
data class ProposalState(
        val amount: Int,
        val buyer: Party,
        val seller: Party,
        val proposer: Party,
        val proposee: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
        override val participants: List<AbstractParty>
        get() = listOf(proposer, proposee)
}
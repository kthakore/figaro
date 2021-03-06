/*
 * ChainFactorTest.scala 
 * ChainFactor tests.
 * 
 * Created By:      Glenn Takata (gtakata@cra.com)
 * Creation Date:   Mar 24, 2015
 * 
 * Copyright 2015 Avrom J. Pfeffer and Charles River Analytics, Inc.
 * See http://www.cra.com or email figaro@cra.com for information.
 * 
 * See http://www.github.com/p2t2/figaro for a copy of the software license.
 */

package com.cra.figaro.test.algorithm.factored.factors.factory

import org.scalatest.Matchers
import org.scalatest.PrivateMethodTester
import org.scalatest.WordSpec
import com.cra.figaro.algorithm.Values
import com.cra.figaro.algorithm.factored.factors.{ SumProductSemiring, Semiring, Variable }
import com.cra.figaro.algorithm.lazyfactored.LazyValues
import com.cra.figaro.algorithm.lazyfactored.Regular
import com.cra.figaro.algorithm.lazyfactored.ValueSet
import com.cra.figaro.algorithm.sampling.ProbEvidenceSampler
import com.cra.figaro.language.Apply
import com.cra.figaro.language.Apply3
import com.cra.figaro.language.Apply4
import com.cra.figaro.language.Apply5
import com.cra.figaro.language.CachingChain
import com.cra.figaro.language.Chain
import com.cra.figaro.language.Condition
import com.cra.figaro.language.Constant
import com.cra.figaro.language.Dist
import com.cra.figaro.language.Flip
import com.cra.figaro.language.Inject
import com.cra.figaro.language.Name.stringToName
import com.cra.figaro.language.NamedEvidence
import com.cra.figaro.language.Reference.stringToReference
import com.cra.figaro.language.Select
import com.cra.figaro.language.Universe
import com.cra.figaro.library.atomic.continuous.Normal
import com.cra.figaro.library.atomic.continuous.Uniform
import com.cra.figaro.library.compound.CPD
import com.cra.figaro.algorithm.factored.ParticleGenerator
import com.cra.figaro.algorithm.factored.factors.Factory

class ChainFactorTest extends WordSpec with Matchers with PrivateMethodTester {

  "Making factors from an element" when {

    "given a chain" should {
      "produce a selector factor over the parent variables" in {
        Universe.createNew()
        val v1 = Flip(0.2)
        val v2 = Select(0.1 -> 1, 0.9 -> 2)
        val v3 = Constant(3)
        val v4 = Chain(v1, (b: Boolean) => if (b) v2; else v3)
        Values()(v4)
        val v1Vals = Variable(v1).range
        val v2Vals = Variable(v2).range
        val v4Vals = Variable(v4).range
        val v1t = v1Vals indexOf Regular(true)
        val v1f = v1Vals indexOf Regular(false)
        val v21 = v2Vals indexOf Regular(1)
        val v22 = v2Vals indexOf Regular(2)
        val v41 = v4Vals indexOf Regular(1)
        val v42 = v4Vals indexOf Regular(2)
        val v43 = v4Vals indexOf Regular(3)

        val factor = Factory.make(v4)
        val v4Factor = factor(0)

        v4Factor.get(List(v1t, 0, v41)) should equal(1.0)
        v4Factor.get(List(v1t, 0, v42)) should equal(0.0)
        v4Factor.get(List(v1t, 0, v43)) should equal(0.0)
        v4Factor.get(List(v1t, 1, v41)) should equal(0.0)
        v4Factor.get(List(v1t, 1, v42)) should equal(1.0)
        v4Factor.get(List(v1t, 1, v43)) should equal(0.0)
        v4Factor.get(List(v1t, 2, v41)) should equal(0.0)
        v4Factor.get(List(v1t, 2, v42)) should equal(0.0)
        v4Factor.get(List(v1t, 2, v43)) should equal(1.0)
        v4Factor.get(List(v1f, 3, v41)) should equal(1.0)
        v4Factor.get(List(v1f, 3, v42)) should equal(0.0)
        v4Factor.get(List(v1f, 3, v43)) should equal(0.0)
        v4Factor.get(List(v1f, 4, v41)) should equal(0.0)
        v4Factor.get(List(v1f, 4, v42)) should equal(1.0)
        v4Factor.get(List(v1f, 4, v43)) should equal(0.0)
        v4Factor.get(List(v1f, 5, v41)) should equal(0.0)
        v4Factor.get(List(v1f, 5, v42)) should equal(0.0)
        v4Factor.get(List(v1f, 5, v43)) should equal(1.0)
      }

      "produce a conditional selector for each dependent element" in {
        Universe.createNew()
        val v1 = Flip(0.2)
        val v2 = Select(0.1 -> 1, 0.9 -> 2)
        val v3 = Constant(3)
        val v4 = Chain(v1, (b: Boolean) => if (b) v2; else v3)
        Values()(v4)
        val v1Vals = Variable(v1).range
        val v2Vals = Variable(v2).range
        val v4Vals = Variable(v4).range
        val v1t = v1Vals indexOf Regular(true)
        val v1f = v1Vals indexOf Regular(false)
        val v21 = v2Vals indexOf Regular(1)
        val v22 = v2Vals indexOf Regular(2)
        val v41 = v4Vals indexOf Regular(1)
        val v42 = v4Vals indexOf Regular(2)
        val v43 = v4Vals indexOf Regular(3)

        val factor = Factory.make(v4)
        val v2Factor = factor(1)

        v2Factor.get(List(0, v21)) should equal(1.0)
        v2Factor.get(List(0, v22)) should equal(0.0)
        v2Factor.get(List(1, v21)) should equal(0.0)
        v2Factor.get(List(1, v22)) should equal(1.0)
        v2Factor.get(List(2, v21)) should equal(0.0)
        v2Factor.get(List(2, v22)) should equal(0.0)

        // Don't cares
        v2Factor.get(List(3, v21)) should equal(1.0)
        v2Factor.get(List(3, v22)) should equal(1.0)
        v2Factor.get(List(4, v21)) should equal(1.0)
        v2Factor.get(List(4, v22)) should equal(1.0)
        v2Factor.get(List(5, v21)) should equal(1.0)
        v2Factor.get(List(5, v22)) should equal(1.0)

        val v3Factor = factor(2)

        v3Factor.get(List(3, 0)) should equal(0.0)
        v3Factor.get(List(4, 0)) should equal(0.0)
        v3Factor.get(List(5, 0)) should equal(1.0)

        // Don't cares
        v3Factor.get(List(0, 0)) should equal(1.0)
        v3Factor.get(List(1, 0)) should equal(1.0)
        v3Factor.get(List(2, 0)) should equal(1.0)

      }
    }

    "given a CPD with one argument" should {
      "produce a selector factor over the parent variables" in {
        Universe.createNew()
        val v1 = Flip(0.2)

        val v2 = CPD(v1, false -> Flip(0.1), true -> Flip(0.7))
        Values()(v2)

        val v1Vals = Variable(v1).range
        val v2Vals = Variable(v2).range

        val v1t = v1Vals indexOf Regular(true)
        val v1f = v1Vals indexOf Regular(false)
        val v2t = v2Vals indexOf Regular(true)
        val v2f = v2Vals indexOf Regular(false)
        val v3t = 0
        val v3f = 1
        val v4t = 0
        val v4f = 1

        val factor = Factory.make(v2)
        val v2Factor = factor(0)

        v2Factor.get(List(v1t, 0, v2t)) should equal(1.0)
        v2Factor.get(List(v1t, 0, v2f)) should equal(0.0)
        v2Factor.get(List(v1t, 1, v2t)) should equal(0.0)
        v2Factor.get(List(v1t, 1, v2f)) should equal(1.0)
        v2Factor.get(List(v1t, 2, v2t)) should equal(0.0)
        v2Factor.get(List(v1t, 2, v2f)) should equal(0.0)
        v2Factor.get(List(v1t, 3, v2t)) should equal(0.0)
        v2Factor.get(List(v1t, 3, v2f)) should equal(0.0)
        v2Factor.get(List(v1f, 0, v2t)) should equal(0.0)
        v2Factor.get(List(v1f, 0, v2f)) should equal(0.0)
        v2Factor.get(List(v1f, 1, v2t)) should equal(0.0)
        v2Factor.get(List(v1f, 1, v2f)) should equal(0.0)
        v2Factor.get(List(v1f, 2, v2t)) should equal(1.0)
        v2Factor.get(List(v1f, 2, v2f)) should equal(0.0)
        v2Factor.get(List(v1f, 3, v2t)) should equal(0.0)
        v2Factor.get(List(v1f, 3, v2f)) should equal(1.0)
      }

      "produce a conditional selector for each dependent element" in {
        Universe.createNew()
        val v1 = Flip(0.2)

        val v2 = CPD(v1, false -> Flip(0.1), true -> Flip(0.7))
        Values()(v2)

        val v1Vals = Variable(v1).range
        val v2Vals = Variable(v2).range

        val v1t = v1Vals indexOf Regular(true)
        val v1f = v1Vals indexOf Regular(false)
        val v2t = v2Vals indexOf Regular(true)
        val v2f = v2Vals indexOf Regular(false)
        val v3t = 0
        val v3f = 1
        val v4t = 0
        val v4f = 1

        val factor = Factory.make(v2)
        val v3Factor = factor(1)

        v3Factor.get(List(0, v3t)) should equal(1.0)
        v3Factor.get(List(0, v3f)) should equal(0.0)
        v3Factor.get(List(1, v3t)) should equal(0.0)
        v3Factor.get(List(1, v3f)) should equal(1.0)

        // Don't cares
        v3Factor.get(List(2, v3t)) should equal(1.0)
        v3Factor.get(List(2, v3f)) should equal(1.0)
        v3Factor.get(List(3, v3t)) should equal(1.0)
        v3Factor.get(List(3, v3f)) should equal(1.0)

        val v4Factor = factor(2)

        v4Factor.get(List(2, v4t)) should equal(1.0)
        v4Factor.get(List(2, v4f)) should equal(0.0)
        v4Factor.get(List(3, v4t)) should equal(0.0)
        v4Factor.get(List(3, v4f)) should equal(1.0)

        // Don't cares
        v4Factor.get(List(0, v4t)) should equal(1.0)
        v4Factor.get(List(0, v4f)) should equal(1.0)
        v4Factor.get(List(1, v4t)) should equal(1.0)
        v4Factor.get(List(1, v4f)) should equal(1.0)

      }
    }
  }
  
}

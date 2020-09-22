/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 *
 */
package de.mpi.ds.matsim_bimodal.drt.analysis;

import org.matsim.api.core.v01.Id;
import de.mpi.ds.matsim_bimodal.drt.passenger.events.DrtRequestSubmittedEvent;
import de.mpi.ds.matsim_bimodal.drt.passenger.events.DrtRequestSubmittedEventHandler;
import de.mpi.ds.matsim_bimodal.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates PerformedRequestEventSequence (for scheduled requests) and RejectedRequestEventSequence (for rejected requests).
 * Almost all data for request/trip analysis is there (except info on actual paths), so should be quite reusable.
 *
 * @author jbischoff
 * @author Michal Maciejewski
 */
public class DrtRequestAnalyzer implements PassengerRequestRejectedEventHandler, PassengerRequestScheduledEventHandler,
		DrtRequestSubmittedEventHandler, PassengerPickedUpEventHandler, PassengerDroppedOffEventHandler {

	public static class PerformedRequestEventSequence {
		private final DrtRequestSubmittedEvent submitted;
		private final PassengerRequestScheduledEvent scheduled;
		private PassengerPickedUpEvent pickedUp;
		private PassengerDroppedOffEvent droppedOff;

		public PerformedRequestEventSequence(DrtRequestSubmittedEvent submitted,
				PassengerRequestScheduledEvent scheduled) {
			this.submitted = submitted;
			this.scheduled = scheduled;
		}

		public DrtRequestSubmittedEvent getSubmitted() {
			return submitted;
		}

		public PassengerRequestScheduledEvent getScheduled() {
			return scheduled;
		}

		public PassengerPickedUpEvent getPickedUp() {
			return pickedUp;
		}

		public PassengerDroppedOffEvent getDroppedOff() {
			return droppedOff;
		}
	}

	public static class RejectedRequestEventSequence {
		private final DrtRequestSubmittedEvent submitted;
		private final PassengerRequestRejectedEvent rejected;

		public RejectedRequestEventSequence(DrtRequestSubmittedEvent submitted,
				PassengerRequestRejectedEvent rejected) {
			this.submitted = submitted;
			this.rejected = rejected;
		}

		public DrtRequestSubmittedEvent getSubmitted() {
			return submitted;
		}

		public PassengerRequestRejectedEvent getRejected() {
			return rejected;
		}
	}

	private final DrtConfigGroup drtCfg;
	private final Map<Id<Request>, DrtRequestSubmittedEvent> requestSubmissions = new HashMap<>();
	private final Map<Id<Request>, RejectedRequestEventSequence> rejectedRequestSequences = new HashMap<>();
	private final Map<Id<Request>, PerformedRequestEventSequence> performedRequestSequences = new HashMap<>();

	public DrtRequestAnalyzer(DrtConfigGroup drtCfg) {
		this.drtCfg = drtCfg;
	}

	public Map<Id<Request>, DrtRequestSubmittedEvent> getRequestSubmissions() {
		return requestSubmissions;
	}

	public Map<Id<Request>, RejectedRequestEventSequence> getRejectedRequestSequences() {
		return rejectedRequestSequences;
	}

	public Map<Id<Request>, PerformedRequestEventSequence> getPerformedRequestSequences() {
		return performedRequestSequences;
	}

	@Override
	public void reset(int iteration) {
		requestSubmissions.clear();
		rejectedRequestSequences.clear();
		performedRequestSequences.clear();
	}

	@Override
	public void handleEvent(DrtRequestSubmittedEvent event) {
		if (event.getMode().equals(drtCfg.getMode())) {
			requestSubmissions.put(event.getRequestId(), event);
		}
	}

	@Override
	public void handleEvent(PassengerRequestScheduledEvent event) {
		if (event.getMode().equals(drtCfg.getMode())) {
			performedRequestSequences.put(event.getRequestId(),
					new PerformedRequestEventSequence(requestSubmissions.get(event.getRequestId()), event));
		}
	}

	@Override
	public void handleEvent(PassengerRequestRejectedEvent event) {
		if (event.getMode().equals(drtCfg.getMode())) {
			rejectedRequestSequences.put(event.getRequestId(),
					new RejectedRequestEventSequence(requestSubmissions.get(event.getRequestId()), event));
		}
	}

	@Override
	public void handleEvent(PassengerPickedUpEvent event) {
		if (event.getMode().equals(drtCfg.getMode())) {
			performedRequestSequences.get(event.getRequestId()).pickedUp = event;
		}
	}

	@Override
	public void handleEvent(PassengerDroppedOffEvent event) {
		if (event.getMode().equals(drtCfg.getMode())) {
			performedRequestSequences.get(event.getRequestId()).droppedOff = event;
		}
	}
}

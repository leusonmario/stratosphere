/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package eu.stratosphere.nephele.io;

import java.io.IOException;

import eu.stratosphere.nephele.template.AbstractOutputTask;
import eu.stratosphere.nephele.template.AbstractTask;
import eu.stratosphere.nephele.types.Record;

public class MutableRecordReader<T extends Record> extends AbstractSingleGateRecordReader<T> implements MutableReader<T> {
	
	private boolean endOfStream;
	
	
	/**
	 * Constructs a new mutable record reader and registers a new input gate with the application's environment.
	 * 
	 * @param taskBase The application that instantiated the record reader.
	 */
	public MutableRecordReader(final AbstractTask taskBase) {
		super(taskBase, MutableRecordDeserializerFactory.<T>get(), 0);
	}

	/**
	 * Constructs a new record reader and registers a new input gate with the application's environment.
	 * 
	 * @param outputBase The application that instantiated the record reader.
	 */
	public MutableRecordReader(final AbstractOutputTask outputBase) {
		super(outputBase, MutableRecordDeserializerFactory.<T>get(), 0);
	}

	/**
	 * Constructs a new record reader and registers a new input gate with the application's environment.
	 * 
	 * @param taskBase
	 *        the application that instantiated the record reader
	 * @param inputGateID
	 *        The ID of the input gate that the reader reads from.
	 */
	public MutableRecordReader(final AbstractTask taskBase, final int inputGateID) {
		super(taskBase, MutableRecordDeserializerFactory.<T>get(), inputGateID);
	}

	/**
	 * Constructs a new record reader and registers a new input gate with the application's environment.
	 * 
	 * @param outputBase
	 *        the application that instantiated the record reader
	 * @param inputGateID
	 *        The ID of the input gate that the reader reads from.
	 */
	public MutableRecordReader(final AbstractOutputTask outputBase, final int inputGateID) {
		super(outputBase, MutableRecordDeserializerFactory.<T>get(), inputGateID);
	}
	
	// --------------------------------------------------------------------------------------------
	
	@Override
	public boolean next(final T target) throws IOException, InterruptedException {
		if (this.endOfStream) {
			return false;
			
		}
		while (true) {
			InputChannelResult result = this.inputGate.readRecord(target);
			switch (result) {
				case INTERMEDIATE_RECORD_FROM_BUFFER:
				case LAST_RECORD_FROM_BUFFER:
					return true;
					
				case EVENT:
					handleEvent(this.inputGate.getCurrentEvent());
					break;	// fall through to get next record
				
				case END_OF_STREAM:
					this.endOfStream = true;
					return false;
					
				default:
					; // fall through to get next record
			}
		}
	}
}

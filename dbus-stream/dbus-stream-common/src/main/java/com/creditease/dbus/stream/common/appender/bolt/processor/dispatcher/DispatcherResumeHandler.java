/*-
 * <<
 * DBus
 * ==
 * Copyright (C) 2016 - 2018 Bridata
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.dbus.stream.common.appender.bolt.processor.dispatcher;

import com.creditease.dbus.stream.common.appender.bolt.processor.BoltCommandHandler;
import com.creditease.dbus.stream.common.appender.bolt.processor.listener.CommandHandlerListener;
import com.creditease.dbus.stream.common.appender.enums.Command;
import com.creditease.dbus.stream.common.Constants;
import com.creditease.dbus.stream.common.appender.bean.EmitData;
import com.creditease.dbus.stream.common.appender.spout.cmds.TopicResumeCmd;
import com.google.common.base.Joiner;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理命令的处理器
 * Created by Shrimp on 16/7/1.
 */
public class DispatcherResumeHandler implements BoltCommandHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private CommandHandlerListener listener;

    public DispatcherResumeHandler(CommandHandlerListener listener) {
        this.listener = listener;
    }

    @Override
    public void handle(Tuple tuple) {
        EmitData emitData = (EmitData) tuple.getValueByField(Constants.EmitFields.DATA);
        Command cmd = (Command)tuple.getValueByField(Constants.EmitFields.COMMAND);

        TopicResumeCmd ctrlCmd = emitData.get(EmitData.CTRL_CMD);
        this.emit(listener.getOutputCollector(), tuple, Joiner.on(".").join(ctrlCmd.getSchema(), ctrlCmd.getTable()), emitData, cmd);
        logger.info("Resume table[{}]", groupField(ctrlCmd.getSchema(), ctrlCmd.getTable()));
    }
}

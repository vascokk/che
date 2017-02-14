/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
/**
 * Error message used to provide custom exit code
 * @author Florent Benoit
 */
export class ErrorMessage {

    private exitCode? : number = 1;

    private error : any;

    constructor(error: any, exitCode : number) {
        this.error = error;
        if (exitCode) {
            this.exitCode = exitCode;
        }
        console.log("build error message with exit code", exitCode);
    }



    getExitCode() : number {
        return this.exitCode;
    }

    getError() : any {
        return this.error;
    }
}

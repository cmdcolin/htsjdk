/*******************************************************************************
 * Copyright 2013 EMBL-EBI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package htsjdk.samtools;

import htsjdk.samtools.cram.ref.CRAMReferenceSource;
import htsjdk.samtools.cram.structure.CRAMEncodingStrategy;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.Log;

import java.io.OutputStream;

public class CRAMFileWriter extends SAMFileWriterImpl {
    private final CRAMContainerStreamWriter cramContainerStream;
    private final SAMFileHeader samFileHeader;
    private final String fileName;

    private static final Log log = Log.getInstance(CRAMFileWriter.class);

    /**
     * Create a CRAMFileWriter on an output stream. Requires input records to be presorted to match the
     * sort order defined by the input {@code samFileHeader}.
     *
     * @param outputStream where to write the output. Can not be null.
     * @param referenceSource reference source. Can not be null.
     * @param samFileHeader {@link SAMFileHeader} to be used. Can not be null. Sort order is determined by the sortOrder property of this arg.
     * @param fileName used for display in error messages
     *
     * @throws IllegalArgumentException if the {@code outputStream}, {@code referenceSource} or {@code samFileHeader} are null
     */
    public CRAMFileWriter(
            final OutputStream outputStream,
            final CRAMReferenceSource referenceSource,
            final SAMFileHeader samFileHeader,
            final String fileName)
    {
        this(outputStream, null, referenceSource, samFileHeader, fileName); // defaults to presorted == true
    }

    /**
     * Create a CRAMFileWriter and optional index on output streams. Requires input records to be presorted to match the
     * sort order defined by the input {@code samFileHeader}.
     *
     * @param outputStream where to write the output. Can not be null.
     * @param indexOS where to write the output index. Can be null if no index is required.
     * @param referenceSource reference source
     * @param samFileHeader {@link SAMFileHeader} to be used. Can not be null. Sort order is determined by the sortOrder property of this arg.
     * @param fileName used for display in error messages
     *
     * @throws IllegalArgumentException if the {@code outputStream}, {@code referenceSource} or {@code samFileHeader} are null
     */
    public CRAMFileWriter(
            final OutputStream outputStream,
            final OutputStream indexOS,
            final CRAMReferenceSource referenceSource,
            final SAMFileHeader samFileHeader,
            final String fileName)
    {
        this(outputStream, indexOS, true, referenceSource, samFileHeader, fileName); // defaults to presorted==true
    }

    /**
     * Create a CRAMFileWriter and optional index on output streams.
     *
     * @param outputStream where to write the output. Can not be null.
     * @param indexOS where to write the output index. Can be null if no index is required.
     * @param presorted if true records written to this writer must already be sorted in the order specified by the header
     * @param referenceSource reference source
     * @param samFileHeader {@link SAMFileHeader} to be used. Can not be null. Sort order is determined by the sortOrder property of this arg.
     * @param fileName used for display in error message display
     *
     * @throws IllegalArgumentException if the {@code outputStream}, {@code referenceSource} or {@code samFileHeader} are null
     */
    public CRAMFileWriter(final OutputStream outputStream, final OutputStream indexOS, final boolean presorted,
                          final CRAMReferenceSource referenceSource, final SAMFileHeader samFileHeader, final String fileName) {
        this( new CRAMEncodingStrategy(), outputStream, indexOS, presorted, referenceSource, samFileHeader, fileName);
    }

    /**
      * Create a CRAMFileWriter and optional index on output streams.
      *
      * @param encodingStrategy encoding strategy to use when writing
      * @param outputStream where to write the output. Can not be null.
      * @param indexOS where to write the output index. Can be null if no index is required.
      * @param presorted if true records written to this writer must already be sorted in the order specified by the header
      * @param referenceSource reference source
      * @param samFileHeader {@link SAMFileHeader} to be used. Can not be null. Sort order is determined by the sortOrder property of this arg.
      * @param fileName used for display in error message display
      *
      * @throws IllegalArgumentException if the {@code outputStream}, {@code referenceSource} or {@code samFileHeader} are null
      */
    public CRAMFileWriter(
            final CRAMEncodingStrategy encodingStrategy,
            final OutputStream outputStream,
            final OutputStream indexOS,
            final boolean presorted,
            final CRAMReferenceSource referenceSource,
            final SAMFileHeader samFileHeader,
            final String fileName) {
        if (outputStream == null) {
            throw new IllegalArgumentException("CRAMWriter output stream can not be null.");
        }
        if (referenceSource == null) {
            throw new IllegalArgumentException("A reference is required for CRAM writers");
        }
        if (samFileHeader == null) {
            throw new IllegalArgumentException("A valid SAMFileHeader is required for CRAM writers");
        }
        this.samFileHeader = samFileHeader;
        this.fileName = fileName;
        setSortOrder(samFileHeader.getSortOrder(), presorted);
        cramContainerStream = new CRAMContainerStreamWriter(
                encodingStrategy,
                referenceSource,
                samFileHeader,
                outputStream,
                indexOS == null ? null : new CRAMBAIIndexer(indexOS, samFileHeader),
                fileName);
        setHeader(samFileHeader);
    }

    /**
     * Write an alignment record.
     * @param alignment must not be null and must have a valid SAMFileHeader.
     */
    @Override
    protected void writeAlignment(final SAMRecord alignment) {
        cramContainerStream.writeAlignment(alignment);
    }

    @Override
    protected void writeHeader(final String textHeader) {
        writeHeader(new SAMTextHeaderCodec().decode(BufferedLineReader.fromString(textHeader),fileName != null ? fileName : null));
    }

    @Override
    protected void writeHeader(final SAMFileHeader header) {
        // the header must have been previously provided the container stream writer, so this
        // header is unused
        if (!header.equals(samFileHeader)) {
            throw new IllegalArgumentException("Attempt to write a different file header than was previously provided");
        }
        cramContainerStream.writeHeader();
    }

    @Override
    protected void finish() {
        cramContainerStream.finish(true); // flush the last container and issue EOF
    }

    @Override
    protected String getFilename() {
        return fileName;
    }

}

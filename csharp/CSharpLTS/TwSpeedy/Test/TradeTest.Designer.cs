namespace Adaptor
{
    partial class TradeTest
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.lbState = new System.Windows.Forms.Label();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.edSymbol = new System.Windows.Forms.TextBox();
            this.edPrice = new System.Windows.Forms.TextBox();
            this.edQuantity = new System.Windows.Forms.TextBox();
            this.cbSide = new System.Windows.Forms.ComboBox();
            this.cbType = new System.Windows.Forms.ComboBox();
            this.btNew = new System.Windows.Forms.Button();
            this.btAmend = new System.Windows.Forms.Button();
            this.btCancel = new System.Windows.Forms.Button();
            this.label6 = new System.Windows.Forms.Label();
            this.edExchangeOrderId = new System.Windows.Forms.TextBox();
            this.SuspendLayout();
            // 
            // lbState
            // 
            this.lbState.AutoSize = true;
            this.lbState.Location = new System.Drawing.Point(89, 59);
            this.lbState.Name = "lbState";
            this.lbState.Size = new System.Drawing.Size(63, 18);
            this.lbState.TabIndex = 0;
            this.lbState.Text = "Off line";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(89, 143);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(59, 18);
            this.label1.TabIndex = 1;
            this.label1.Text = "Symbol";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(228, 143);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(44, 18);
            this.label2.TabIndex = 2;
            this.label2.Text = "Price";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(348, 143);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(67, 18);
            this.label3.TabIndex = 3;
            this.label3.Text = "Quantity";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(477, 144);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(38, 18);
            this.label4.TabIndex = 4;
            this.label4.Text = "Side";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(618, 143);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(42, 18);
            this.label5.TabIndex = 5;
            this.label5.Text = "Type";
            // 
            // edSymbol
            // 
            this.edSymbol.Location = new System.Drawing.Point(92, 194);
            this.edSymbol.Name = "edSymbol";
            this.edSymbol.Size = new System.Drawing.Size(89, 29);
            this.edSymbol.TabIndex = 6;
            this.edSymbol.Text = "TXFJ5";
            // 
            // edPrice
            // 
            this.edPrice.Location = new System.Drawing.Point(221, 193);
            this.edPrice.Name = "edPrice";
            this.edPrice.Size = new System.Drawing.Size(91, 29);
            this.edPrice.TabIndex = 7;
            this.edPrice.Text = "8230";
            // 
            // edQuantity
            // 
            this.edQuantity.Location = new System.Drawing.Point(351, 194);
            this.edQuantity.Name = "edQuantity";
            this.edQuantity.Size = new System.Drawing.Size(85, 29);
            this.edQuantity.TabIndex = 8;
            this.edQuantity.Text = "5";
            // 
            // cbSide
            // 
            this.cbSide.FormattingEnabled = true;
            this.cbSide.Items.AddRange(new object[] {
            "Buy",
            "Sell"});
            this.cbSide.Location = new System.Drawing.Point(479, 193);
            this.cbSide.Name = "cbSide";
            this.cbSide.Size = new System.Drawing.Size(86, 26);
            this.cbSide.TabIndex = 9;
            this.cbSide.Text = "Buy";
            // 
            // cbType
            // 
            this.cbType.FormattingEnabled = true;
            this.cbType.Items.AddRange(new object[] {
            "Limit",
            "Market"});
            this.cbType.Location = new System.Drawing.Point(615, 194);
            this.cbType.Name = "cbType";
            this.cbType.Size = new System.Drawing.Size(95, 26);
            this.cbType.TabIndex = 10;
            this.cbType.Text = "Limit";
            // 
            // btNew
            // 
            this.btNew.Location = new System.Drawing.Point(92, 305);
            this.btNew.Name = "btNew";
            this.btNew.Size = new System.Drawing.Size(75, 33);
            this.btNew.TabIndex = 11;
            this.btNew.Text = "New";
            this.btNew.UseVisualStyleBackColor = true;
            this.btNew.Click += new System.EventHandler(this.btNew_Click);
            // 
            // btAmend
            // 
            this.btAmend.Location = new System.Drawing.Point(221, 305);
            this.btAmend.Name = "btAmend";
            this.btAmend.Size = new System.Drawing.Size(75, 33);
            this.btAmend.TabIndex = 12;
            this.btAmend.Text = "Amend";
            this.btAmend.UseVisualStyleBackColor = true;
            this.btAmend.Click += new System.EventHandler(this.btAmend_Click);
            // 
            // btCancel
            // 
            this.btCancel.Location = new System.Drawing.Point(367, 305);
            this.btCancel.Name = "btCancel";
            this.btCancel.Size = new System.Drawing.Size(75, 33);
            this.btCancel.TabIndex = 13;
            this.btCancel.Text = "Cancel";
            this.btCancel.UseVisualStyleBackColor = true;
            this.btCancel.Click += new System.EventHandler(this.btCancel_Click);
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(744, 143);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(128, 18);
            this.label6.TabIndex = 14;
            this.label6.Text = "ExchangeOrderId";
            // 
            // edExchangeOrderId
            // 
            this.edExchangeOrderId.Location = new System.Drawing.Point(747, 193);
            this.edExchangeOrderId.Name = "edExchangeOrderId";
            this.edExchangeOrderId.Size = new System.Drawing.Size(100, 29);
            this.edExchangeOrderId.TabIndex = 15;
            // 
            // TradeTest
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 18F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(922, 596);
            this.Controls.Add(this.edExchangeOrderId);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.btCancel);
            this.Controls.Add(this.btAmend);
            this.Controls.Add(this.btNew);
            this.Controls.Add(this.cbType);
            this.Controls.Add(this.cbSide);
            this.Controls.Add(this.edQuantity);
            this.Controls.Add(this.edPrice);
            this.Controls.Add(this.edSymbol);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.lbState);
            this.Margin = new System.Windows.Forms.Padding(4);
            this.Name = "TradeTest";
            this.Text = "Trading Test";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.TradeTest_FormClosing);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label lbState;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TextBox edSymbol;
        private System.Windows.Forms.TextBox edPrice;
        private System.Windows.Forms.TextBox edQuantity;
        private System.Windows.Forms.ComboBox cbSide;
        private System.Windows.Forms.ComboBox cbType;
        private System.Windows.Forms.Button btNew;
        private System.Windows.Forms.Button btAmend;
        private System.Windows.Forms.Button btCancel;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.TextBox edExchangeOrderId;
    }
}

